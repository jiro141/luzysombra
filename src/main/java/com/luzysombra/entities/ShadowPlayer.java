package com.luzysombra.entities;

import javafx.scene.input.KeyCode;

/**
 * Sombra: la criatura de la penumbra. Se controla con J/L/I (y K para bajar escaleras).
 * Recoge Obsidianas y su puerta de salida es la puerta negra.
 */
public class ShadowPlayer extends Player {

    public ShadowPlayer(String id, double x, double y) {
        super(id, "shadow_player", x, y);
        this.playerView = new PlayerView(false);
    }

    @Override
    protected KeyCode keyLeft() {
        return KeyCode.J;
    }

    @Override
    protected KeyCode keyRight() {
        return KeyCode.L;
    }

    @Override
    protected KeyCode keyJump() {
        return KeyCode.I;
    }

    @Override
    protected KeyCode keyClimbUp() {
        return KeyCode.I;
    }

    @Override
    protected KeyCode keyClimbDown() {
        return KeyCode.K;
    }
}
