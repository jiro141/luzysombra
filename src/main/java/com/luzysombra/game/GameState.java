package com.luzysombra.game;

import com.luzysombra.config.GameConfig;
import com.luzysombra.entities.Door;
import com.luzysombra.entities.LightPlayer;
import com.luzysombra.entities.ShadowPlayer;

import java.util.HashSet;
import java.util.Set;

/**
 * Estado de la partida en curso: vidas, coleccionables recogidos, puertas,
 * tiempo, condiciones de victoria/derrota y checkpoints activados.
 */
public class GameState {

    private final Level level;
    private final LightPlayer light;
    private final ShadowPlayer shadow;

    private int alborsCollected;
    private int obsidiansCollected;
    private double timeElapsed;
    private boolean levelComplete;
    private boolean gameOver;
    private final Set<String> activatedCheckpoints = new HashSet<>();

    public GameState(Level level, LightPlayer light, ShadowPlayer shadow) {
        this.level = level;
        this.light = light;
        this.shadow = shadow;
    }

    /** Actualiza la lógica del nivel: puertas según coleccionables y temporizador. */
    public void update(double dt) {
        if (isFinished()) {
            return;
        }
        timeElapsed += dt;

        Door lightDoor = level.getLightDoor();
        Door shadowDoor = level.getShadowDoor();
        if (lightDoor != null) {
            lightDoor.setOpen(alborsCollected >= level.countAlbor());
        }
        if (shadowDoor != null) {
            shadowDoor.setOpen(obsidiansCollected >= level.countObsidian());
        }
    }

    public boolean isFinished() {
        return levelComplete || gameOver;
    }

    // ---------------------------------------------------------------
    // Coleccionables
    // ---------------------------------------------------------------

    public void addAlbor() {
        alborsCollected++;
    }

    public void addObsidian() {
        obsidiansCollected++;
    }

    public int getAlborsCollected() {
        return alborsCollected;
    }

    public int getObsidiansCollected() {
        return obsidiansCollected;
    }

    public int getTotalAlbors() {
        return level.countAlbor();
    }

    public int getTotalObsidians() {
        return level.countObsidian();
    }

    // ---------------------------------------------------------------
    // Checkpoints
    // ---------------------------------------------------------------

    public boolean isCheckpointActive(String id) {
        return activatedCheckpoints.contains(id);
    }

    public void activateCheckpoint(String id) {
        activatedCheckpoints.add(id);
    }

    // ---------------------------------------------------------------
    // Condiciones de fin
    // ---------------------------------------------------------------

    public void markGameOver() {
        gameOver = true;
    }

    public void markLevelComplete() {
        levelComplete = true;
    }

    public boolean isLevelComplete() {
        return levelComplete;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Comprueba la condición de victoria: los dos personajes frente a su puerta
     * y con todos sus coleccionables recogidos.
     */
    public boolean checkVictory() {
        boolean allCollected = alborsCollected >= level.countAlbor()
                && obsidiansCollected >= level.countObsidian();
        boolean bothAtDoor = light.hasArrivedAtDoor() && shadow.hasArrivedAtDoor();
        return allCollected && bothAtDoor;
    }

    public double getTimeElapsed() {
        return timeElapsed;
    }

    public Level getLevel() {
        return level;
    }

    public LightPlayer getLight() {
        return light;
    }

    public ShadowPlayer getShadow() {
        return shadow;
    }

    public int getTotalLives() {
        return GameConfig.STARTING_LIVES;
    }
}
