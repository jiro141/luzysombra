package com.luzysombra.entities;

import javafx.scene.input.KeyCode;

/**
 * Luz: el ser de la luz blanca. Se controla con A/D/W (y S para bajar escaleras).
 * Recoge Albores y su puerta de salida es la puerta blanca.
 */
public class LightPlayer extends Player {

    public LightPlayer(String id, double x, double y) {
        super(id, "light_player", x, y);
        this.playerView = new PlayerView(true);
    }

    @Override
    protected KeyCode keyLeft() {
        return KeyCode.A;
    }

    @Override
    protected KeyCode keyRight() {
        return KeyCode.D;
    }

    @Override
    protected KeyCode keyJump() {
        return KeyCode.W;
    }

    @Override
    protected KeyCode keyClimbUp() {
        return KeyCode.W;
    }

    @Override
    protected KeyCode keyClimbDown() {
        return KeyCode.S;
    }
}
