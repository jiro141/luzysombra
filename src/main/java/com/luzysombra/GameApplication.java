package com.luzysombra;

import com.luzysombra.config.GameConfig;
import com.luzysombra.config.ResourcePaths;
import com.luzysombra.input.InputManager;
import com.luzysombra.navigation.ScreenManager;
import com.luzysombra.persistence.ProgressManager;
import com.luzysombra.resources.ResourceManager;
import com.luzysombra.screens.GameScreen;
import com.luzysombra.screens.LevelSelectionScreen;
import com.luzysombra.screens.LoadingScreen;
import com.luzysombra.screens.MainMenuScreen;
import com.luzysombra.screens.TutorialScreen;
import com.luzysombra.ui.ConfirmExitOverlay;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Aplicación JavaFX: UN SOLO Stage principal.
 * <p>
 * La ventana contiene un contenedor de escala que conserva la relación 16:9
 * (resolución lógica 1600x900) con letterboxing al redimensionar. Todas las
 * pantallas y overlays viven dentro de ese contenedor; no existen ventanas
 * secundarias, diálogos ni alerts.
 */
public class GameApplication extends Application {

    private ScreenManager screens;
    private InputManager input;
    private ProgressManager progress;
    private MainMenuScreen mainMenu;
    private LevelSelectionScreen levelSelection;

    @Override
    public void start(Stage stage) {
        // ------------------------------------------------------------
        // Contenedor raíz con escala 16:9 y letterboxing
        // ------------------------------------------------------------
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #05040a;");

        Pane scaleHost = new Pane();
        scaleHost.setPrefSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        scaleHost.setMaxSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        scaleHost.setMinSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        root.getChildren().add(scaleHost);

        // ------------------------------------------------------------
        // Núcleo de la aplicación
        // ------------------------------------------------------------
        screens = new ScreenManager();
        input = new InputManager();
        progress = new ProgressManager();

        scaleHost.getChildren().add(screens.getRoot());

        Scene scene = new Scene(root, GameConfig.WINDOW_INITIAL_WIDTH, GameConfig.WINDOW_INITIAL_HEIGHT);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource(ResourcePaths.STYLE_CSS)).toExternalForm());

        stage.setTitle("Luz y Sombra");
        stage.setMinWidth(GameConfig.WINDOW_MIN_WIDTH);
        stage.setMinHeight(GameConfig.WINDOW_MIN_HEIGHT);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            ResourceManager.getInstance().disposeAll();
            stop();
        });
        stage.show();
        // Primer clic sin foco previo: el Stage reclama el foco al mostrarse.
        stage.requestFocus();

        // ------------------------------------------------------------
        // Escala adaptativa (letterboxing 16:9)
        // ------------------------------------------------------------
        root.widthProperty().addListener((obs, o, n) -> applyScale(root, scaleHost));
        root.heightProperty().addListener((obs, o, n) -> applyScale(root, scaleHost));
        applyScale(root, scaleHost);

        input.attach(scene);

        // ------------------------------------------------------------
        // Navegación inicial: pantalla de carga → menú principal
        // ------------------------------------------------------------
        LoadingScreen loading = new LoadingScreen(this::showMainMenu);
        screens.showScreen(com.luzysombra.navigation.ScreenType.MAIN_MENU, loading);
        loading.startLoading();
    }

    private void applyScale(StackPane root, Pane scaleHost) {
        double w = root.getWidth();
        double h = root.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        double scale = Math.min(w / GameConfig.LOGICAL_WIDTH, h / GameConfig.LOGICAL_HEIGHT);
        scaleHost.setScaleX(scale);
        scaleHost.setScaleY(scale);
        scaleHost.setLayoutX((w - GameConfig.LOGICAL_WIDTH * scale) / 2.0);
        scaleHost.setLayoutY((h - GameConfig.LOGICAL_HEIGHT * scale) / 2.0);
    }

    // ================================================================
    // Navegación entre pantallas
    // ================================================================

    private void showMainMenu() {
        mainMenu = new MainMenuScreen(
                this::showLevelSelection,
                this::showTutorial,
                () -> screens.pushOverlay(new ConfirmExitOverlay(
                        () -> Platform.exit(),
                        () -> screens.popOverlay())));
        mainMenu.syncVolume();
        screens.showScreen(com.luzysombra.navigation.ScreenType.MAIN_MENU, mainMenu);
        mainMenu.startParticles();
    }

    private void showTutorial() {
        screens.showScreen(com.luzysombra.navigation.ScreenType.TUTORIAL,
                new TutorialScreen(this::showMainMenu, this::showLevelSelection));
    }

    private void showLevelSelection() {
        levelSelection = new LevelSelectionScreen(progress,
                this::startLevel, this::showMainMenu);
        screens.showScreen(com.luzysombra.navigation.ScreenType.LEVEL_SELECTION, levelSelection);
        levelSelection.startParticles();
    }

    private void startLevel(int levelNumber) {
        GameScreen game = new GameScreen(
                levelNumber,
                screens,
                input,
                progress,
                this::showMainMenu,
                this::showLevelSelection,
                () -> startLevel(levelNumber + 1));
        screens.showScreen(com.luzysombra.navigation.ScreenType.GAME, game);
    }

    @Override
    public void stop() {
        if (screens != null) {
            screens.clearOverlays();
        }
        ResourceManager.getInstance().disposeAll();
    }
}
