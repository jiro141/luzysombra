package com.luzysombra.game;

import com.luzysombra.config.ResourcePaths;
import com.luzysombra.entities.Collectible;
import com.luzysombra.entities.Door;
import com.luzysombra.entities.Hazard;
import com.luzysombra.entities.MovingPlatform;
import com.luzysombra.entities.Player;
import com.luzysombra.entities.Switch;
import com.luzysombra.input.InputManager;
import com.luzysombra.resources.ResourceManager;
import com.luzysombra.util.CollisionUtils;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Game loop principal del nivel. Corre en el hilo de JavaFX mediante
 * {@link AnimationTimer} (sin Thread.sleep), con delta time limitado para
 * evitar saltos físicos al recuperar el foco de la ventana.
 * <p>
 * Separa las responsabilidades: entrada (InputManager), física y colisiones
 * (CollisionManager), estado (GameState), cámara y render (Group del mundo).
 */
public final class GameLoop {

    /** Callbacks hacia la capa de interfaz (HUD y overlays). */
    public interface Listener {
        void onLivesChanged(int lightLives, int shadowLives);

        void onCollectiblesChanged(int albors, int totalAlbors, int obsidians, int totalObsidians);

        void onPlayerDamaged(Player player);

        void onGameOver();

        void onVictory(double timeSeconds, int albors, int obsidians, int lightLives, int shadowLives);

        /** Se invoca cada frame con el tiempo transcurrido del nivel (para el HUD). */
        void onTick(double timeElapsed);
    }

    private final GameState state;
    private final InputManager input;
    private final Camera camera;
    private final Pane worldGroup;
    private final Listener listener;

    private final AnimationTimer timer = new AnimationTimer() {
        private long lastNano = -1;

        @Override
        public void handle(long now) {
            if (paused || disposed) {
                lastNano = -1;
                return;
            }
            if (lastNano < 0) {
                lastNano = now;
                return;
            }
            double dt = Math.min((now - lastNano) / 1_000_000_000.0,
                    com.luzysombra.config.GameConfig.MAX_DELTA_TIME);
            lastNano = now;
            tick(dt);
        }
    };

    private final List<ParallaxLayer> parallaxLayers = new ArrayList<>();
    private boolean paused;
    private boolean disposed;

    public GameLoop(GameState state, InputManager input, Camera camera,
                    Pane worldGroup, Listener listener) {
        this.state = state;
        this.input = input;
        this.camera = camera;
        this.worldGroup = worldGroup;
        this.listener = listener;
    }

    /** Capa de fondo con factor de parallax (0 = fija, 1 = se mueve con el mundo). */
    public void addParallaxLayer(Node layer, double factor) {
        parallaxLayers.add(new ParallaxLayer(layer, factor));
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
        input.clearAll();
    }

    public boolean isPaused() {
        return paused;
    }

    public void dispose() {
        disposed = true;
        timer.stop();
    }

    // ================================================================
    // Tick principal
    // ================================================================

    private void tick(double dt) {
        if (state.isFinished()) {
            return;
        }

        state.update(dt);

        Player light = state.getLight();
        Player shadow = state.getShadow();

        // Interruptores cooperativos: un personaje activa la plataforma del otro
        for (Switch sw : state.getLevel().getSwitches()) {
            boolean pressed = sw.isPressedBy(light) || sw.isPressedBy(shadow);
            sw.setPressed(pressed);
            MovingPlatform mp = state.getLevel().getMovingPlatformById(sw.getTargetId());
            if (mp != null) {
                mp.setActive(pressed);
            }
        }

        // Plataformas móviles
        double levelTime = state.getTimeElapsed();
        for (MovingPlatform mp : state.getLevel().getMovingPlatforms()) {
            mp.update(levelTime);
        }

        // Entrada → física → colisiones
        light.handleInput(input);
        shadow.handleInput(input);
        light.updatePhysics(dt);
        shadow.updatePhysics(dt);
        CollisionManager.resolve(light, state.getLevel(), dt);
        CollisionManager.resolve(shadow, state.getLevel(), dt);

        // Eventos del mundo
        checkHazards(light);
        checkHazards(shadow);
        checkFall(light);
        checkFall(shadow);
        checkCollectibles(light);
        checkCollectibles(shadow);
        checkDoors(light);
        checkDoors(shadow);
        checkCheckpoints(light);
        checkCheckpoints(shadow);

        // Animación visual y cámara
        light.updateVisual(dt);
        shadow.updateVisual(dt);
        camera.update(dt, light, shadow);
        applyCamera();

        // Condición de victoria
        if (state.checkVictory()) {
            state.markLevelComplete();
            ResourceManager.getInstance().playSound(ResourcePaths.SOUND_VICTORY);
            listener.onVictory(state.getTimeElapsed(),
                    state.getAlborsCollected(), state.getObsidiansCollected(),
                    light.getLives(), shadow.getLives());
        }

        listener.onTick(state.getTimeElapsed());
    }

    private void applyCamera() {
        worldGroup.setTranslateX(-camera.getX());
        worldGroup.setTranslateY(-camera.getY());
        for (ParallaxLayer layer : parallaxLayers) {
            layer.node.setTranslateX(-camera.getX() * layer.factor);
            layer.node.setTranslateY(-camera.getY() * layer.factor);
        }
    }

    // ================================================================
    // Eventos
    // ================================================================

    private void checkHazards(Player player) {
        if (player.isInvulnerable() || player.isInHitStun()) {
            return;
        }
        for (Hazard hazard : state.getLevel().getHazards()) {
            if (hazard.damages(player) && intersects(player, hazard)) {
                boolean died = player.takeDamage();
                listener.onLivesChanged(state.getLight().getLives(), state.getShadow().getLives());
                if (died) {
                    state.markGameOver();
                    ResourceManager.getInstance().playSound(ResourcePaths.SOUND_GAME_OVER);
                    listener.onGameOver();
                    return;
                }
                listener.onPlayerDamaged(player);
                return;
            }
        }
    }

    private void checkFall(Player player) {
        if (player.getY() > state.getLevel().getHeight() + 80) {
            boolean died = player.takeDamage();
            listener.onLivesChanged(state.getLight().getLives(), state.getShadow().getLives());
            if (died) {
                state.markGameOver();
                ResourceManager.getInstance().playSound(ResourcePaths.SOUND_GAME_OVER);
                listener.onGameOver();
                return;
            }
            listener.onPlayerDamaged(player);
        }
    }

    private void checkCollectibles(Player player) {
        for (Collectible collectible : state.getLevel().getCollectibles()) {
            if (collectible.isCollected() || !collectible.canCollect(player)
                    || !intersects(player, collectible)) {
                continue;
            }
            collectible.collect();
            if (collectible.getCollectibleType() == Collectible.CollectibleType.ALBOR) {
                state.addAlbor();
                ResourceManager.getInstance().playSound(ResourcePaths.SOUND_COLLECT_LIGHT);
            } else {
                state.addObsidian();
                ResourceManager.getInstance().playSound(ResourcePaths.SOUND_COLLECT_SHADOW);
            }
            listener.onCollectiblesChanged(
                    state.getAlborsCollected(), state.getTotalAlbors(),
                    state.getObsidiansCollected(), state.getTotalObsidians());
            animateCollection(collectible);
        }
    }

    private void checkDoors(Player player) {
        for (Door door : state.getLevel().getDoors()) {
            if (!door.isOpen() || !door.accepts(player) || !intersects(player, door)) {
                continue;
            }
            if (!player.hasArrivedAtDoor()) {
                player.setArrivedAtDoor(true);
                ResourceManager.getInstance().playSound(ResourcePaths.SOUND_DOOR);
            }
        }
    }

    private void checkCheckpoints(Player player) {
        for (var checkpoint : state.getLevel().getCheckpoints()) {
            if (state.isCheckpointActive(checkpoint.getId()) || !intersects(player, checkpoint)) {
                continue;
            }
            state.activateCheckpoint(checkpoint.getId());
            checkpoint.activate();
            player.setRespawn(checkpoint.getCenterX(), checkpoint.getCenterY());
            ResourceManager.getInstance().playSound(ResourcePaths.SOUND_CHECKPOINT);
        }
    }

    private void animateCollection(Collectible collectible) {
        Node view = collectible.getView();
        FadeTransition fade = new FadeTransition(Duration.millis(280), view);
        fade.setToValue(0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(280), view);
        scale.setToX(0.2);
        scale.setToY(0.2);
        ParallelTransition pt = new ParallelTransition(fade, scale);
        pt.setOnFinished(e -> {
            // Los nodos del nivel son hijos directos del worldGroup (Pane).
            // Usamos getParent() para removerlo del contenedor correcto.
            if (view.getParent() instanceof Pane parent) {
                parent.getChildren().remove(view);
            }
        });
        pt.play();
    }

    private boolean intersects(Player player, com.luzysombra.entities.GameObject obj) {
        return CollisionUtils.intersects(
                player.getX(), player.getY(), player.getWidth(), player.getHeight(),
                obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight());
    }

    /** Capa de fondo con su factor de parallax. */
    private static final class ParallaxLayer {
        final Node node;
        final double factor;

        ParallaxLayer(Node node, double factor) {
            this.node = node;
            this.factor = factor;
        }
    }
}
