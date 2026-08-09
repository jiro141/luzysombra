package com.luzysombra.screens;

import com.luzysombra.config.GameConfig;
import com.luzysombra.config.ResourcePaths;
import com.luzysombra.entities.LightPlayer;
import com.luzysombra.entities.Player;
import com.luzysombra.entities.ShadowPlayer;
import com.luzysombra.game.Camera;
import com.luzysombra.game.GameLoop;
import com.luzysombra.game.GameState;
import com.luzysombra.game.Level;
import com.luzysombra.game.LevelLoader;
import com.luzysombra.input.InputManager;
import com.luzysombra.navigation.ScreenManager;
import com.luzysombra.persistence.ProgressManager;
import com.luzysombra.resources.ResourceManager;
import com.luzysombra.ui.GameHud;
import com.luzysombra.ui.GameOverOverlay;
import com.luzysombra.ui.PauseOverlay;
import com.luzysombra.ui.VictoryOverlay;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;

/**
 * Pantalla de juego: mundo del nivel, personajes, HUD y overlays internos
 * (pausa, derrota, victoria). Toda la interfaz vive dentro del mismo StackPane;
 * nunca se abre una ventana nueva.
 */
public class GameScreen extends StackPane {

    private final int levelNumber;
    private final ScreenManager screens;
    private final InputManager input;
    private final ProgressManager progress;
    private final Runnable onExitToMenu;
    private final Runnable onExitToLevelSelect;
    private final Runnable onNextLevel;

    private final StackPane backgroundLayer = new StackPane();
    // Pane con tamaño fijo = resolución lógica. El StackPane lo coloca en
    // (0,0) sin centrarlo (a diferencia de Group cuyo tamaño depende del contenido).
    private final Pane worldGroup = new Pane();
    private final GameHud hud = new GameHud(this::requestPause);

    private Level level;
    private GameState state;
    private Camera camera;
    private GameLoop gameLoop;
    private PauseOverlay pauseOverlay;
    private GameOverOverlay gameOverOverlay;
    private VictoryOverlay victoryOverlay;

    private boolean busy;

    public GameScreen(int levelNumber, ScreenManager screens, InputManager input,
                      ProgressManager progress, Runnable onExitToMenu,
                      Runnable onExitToLevelSelect, Runnable onNextLevel) {
        this.levelNumber = levelNumber;
        this.screens = screens;
        this.input = input;
        this.progress = progress;
        this.onExitToMenu = onExitToMenu;
        this.onExitToLevelSelect = onExitToLevelSelect;
        this.onNextLevel = onNextLevel;

        setPrefSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        getStyleClass().add("game-screen");

        buildBackground();

        // Pane de tamaño fijo: el StackPane lo centra según prefSize (=resolución
        // lógica), por lo que siempre queda en (0,0) del GameScreen. El clip
        // coincide exactamente con el viewport visible.
        worldGroup.setPrefSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        worldGroup.setMaxSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        worldGroup.setMinSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        Rectangle worldClip = new Rectangle(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        worldClip.setFill(Color.TRANSPARENT);
        worldGroup.setClip(worldClip);

        hud.setMaxWidth(GameConfig.LOGICAL_WIDTH);
        hud.setMaxHeight(90);
        StackPane.setAlignment(hud, javafx.geometry.Pos.TOP_CENTER);

        getChildren().addAll(backgroundLayer, worldGroup, hud);

        input.setOnEdgePress(this::handleGlobalKey);
        buildLevel();
    }

    // ================================================================
    // Construcción
    // ================================================================

    private void buildBackground() {
        backgroundLayer.setMouseTransparent(true);
        // Dos capas de parallax: lejana y cercana
        backgroundLayer.getChildren().addAll(
                makeBackgroundTile(ResourcePaths.backgroundLevel(levelNumber), 0.12),
                makeBackgroundTile(ResourcePaths.backgroundLevel(levelNumber), 0.3));
    }

    /** Crea un panel con el fondo repetido horizontalmente (tile de 3 copias). */
    private Node makeBackgroundTile(String path, double factor) {
        Pane tile = new Pane();
        tile.setMouseTransparent(true);
        tile.setPrefSize(GameConfig.LOGICAL_WIDTH * 3, GameConfig.LOGICAL_HEIGHT);
        for (int i = 0; i < 3; i++) {
            ImageView img = new ImageView(ResourceManager.getInstance().getImage(path));
            img.setFitWidth(GameConfig.LOGICAL_WIDTH);
            img.setFitHeight(GameConfig.LOGICAL_HEIGHT);
            img.setPreserveRatio(false);
            img.setLayoutX(i * GameConfig.LOGICAL_WIDTH);
            tile.getChildren().add(img);
        }
        // El factor de parallax se registra con el loop; por ahora lo guardamos como propiedad
        tile.setUserData(factor);
        return tile;
    }

    private void buildLevel() {
        level = LevelLoader.load(levelNumber);

        LightPlayer light = new LightPlayer("luz", level.getSpawnLightX(), level.getSpawnLightY());
        ShadowPlayer shadow = new ShadowPlayer("sombra", level.getSpawnShadowX(), level.getSpawnShadowY());
        light.setLevelBounds(level.getWidth(), level.getHeight());
        shadow.setLevelBounds(level.getWidth(), level.getHeight());
        light.setRespawn(level.getSpawnLightX(), level.getSpawnLightY());
        shadow.setRespawn(level.getSpawnShadowX(), level.getSpawnShadowY());

        state = new GameState(level, light, shadow);
        camera = new Camera();
        camera.setLevelBounds(level.getWidth(), level.getHeight());

        // Se copian los nodos del nivel al worldGroup usando new ArrayList para
        // evitar ConcurrentModificationException al iterar la ObservableList viva.
        // Los nodos ya tienen layoutX/Y en coordenadas del mundo desde getView().
        worldGroup.getChildren().setAll(new ArrayList<>(level.buildWorld().getChildren()));
        worldGroup.getChildren().addAll(light.getView(), shadow.getView());

        gameLoop = new GameLoop(state, input, camera, worldGroup, listener());
        // Capas de parallax ya agregadas en el fondo
        for (Node node : backgroundLayer.getChildren()) {
            double factor = (double) node.getUserData();
            gameLoop.addParallaxLayer(node, factor);
        }

        hud.setLevelName(level.getName());
        hud.setLives(light.getLives(), shadow.getLives());
        hud.setCollectibles(0, level.countAlbor(), 0, level.countObsidian());

        pauseOverlay = new PauseOverlay(this::resume, this::restartLevel, this::goToMenu);
        gameOverOverlay = new GameOverOverlay(this::restartLevel, this::goToLevelSelect, this::goToMenu);
        victoryOverlay = new VictoryOverlay(this::nextLevel, this::restartLevel,
                this::goToLevelSelect, levelNumber < GameConfig.TOTAL_LEVELS);

        gameLoop.start();
    }

    private GameLoop.Listener listener() {
        return new GameLoop.Listener() {
            @Override
            public void onLivesChanged(int lightLives, int shadowLives) {
                hud.setLives(lightLives, shadowLives);
            }

            @Override
            public void onCollectiblesChanged(int albors, int totalAlbors, int obsidians, int totalObsidians) {
                hud.setCollectibles(albors, totalAlbors, obsidians, totalObsidians);
            }

            @Override
            public void onPlayerDamaged(Player player) {
                // La animación de daño la maneja la vista del personaje
            }

            @Override
            public void onGameOver() {
                gameLoop.pause();
                input.setEnabled(false);
                screens.pushOverlay(gameOverOverlay);
            }

            @Override
            public void onVictory(double timeSeconds, int albors, int obsidians, int lightLives, int shadowLives) {
                progress.recordResult(levelNumber, timeSeconds, albors + obsidians);
                victoryOverlay.setStats(timeSeconds, albors, obsidians, lightLives, shadowLives);
                gameLoop.pause();
                input.setEnabled(false);
                screens.pushOverlay(victoryOverlay);
            }

            @Override
            public void onTick(double timeElapsed) {
                hud.setTime(timeElapsed);
            }
        };
    }

    // ================================================================
    // Pausa / reanudación
    // ================================================================

    private void handleGlobalKey(KeyCode code) {
        if (code == KeyCode.ESCAPE) {
            requestPause();
        }
    }

    private void requestPause() {
        if (busy || gameLoop == null || state == null || state.isFinished()) {
            return;
        }
        gameLoop.pause();
        input.setEnabled(false);
        screens.pushOverlay(pauseOverlay);
    }

    private void resume() {
        screens.popOverlay();
        input.setEnabled(true);
        gameLoop.resume();
    }

    private void restartLevel() {
        if (busy) {
            return;
        }
        busy = true;
        screens.popOverlay();
        disposeLevel();
        buildLevel();
        input.setEnabled(true);
        busy = false;
    }

    private void nextLevel() {
        if (levelNumber >= GameConfig.TOTAL_LEVELS) {
            return;
        }
        disposeLevel();
        onNextLevel.run();
    }

    private void goToLevelSelect() {
        disposeLevel();
        onExitToLevelSelect.run();
    }

    private void goToMenu() {
        disposeLevel();
        onExitToMenu.run();
    }

    private void disposeLevel() {
        if (gameLoop != null) {
            gameLoop.dispose();
        }
        input.setEnabled(true);
        input.clearAll();
        screens.clearOverlays();
        worldGroup.getChildren().clear();
    }

    public void disposeScreen() {
        disposeLevel();
        input.setOnEdgePress(null);
    }
}
