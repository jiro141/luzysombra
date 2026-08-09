package com.luzysombra.game;

import com.luzysombra.config.GameConfig;
import com.luzysombra.entities.Player;

/**
 * Cámara del nivel: sigue el punto medio entre ambos jugadores con suavizado
 * (lerp) y se mantiene dentro de los límites del nivel y de la ventana lógica.
 * La posición de la cámara se aplica como translación negativa al Group del mundo.
 */
public class Camera {

    private double x;
    private double y;
    private double levelWidth;
    private double levelHeight;

    public void setLevelBounds(double levelWidth, double levelHeight) {
        this.levelWidth = levelWidth;
        this.levelHeight = levelHeight;
    }

    public void update(double dt, Player light, Player shadow) {
        double targetX = (light.getCenterX() + shadow.getCenterX()) / 2.0 - GameConfig.LOGICAL_WIDTH / 2.0;
        double targetY = (light.getCenterY() + shadow.getCenterY()) / 2.0 - GameConfig.LOGICAL_HEIGHT / 2.0;

        double t = Math.min(1.0, GameConfig.CAMERA_SMOOTHING * dt);
        x += (targetX - x) * t;
        y += (targetY - y) * t;

        // Límites: no mostrar fuera del nivel
        double maxX = Math.max(0, levelWidth - GameConfig.LOGICAL_WIDTH);
        double maxY = Math.max(0, levelHeight - GameConfig.LOGICAL_HEIGHT);
        x = Math.max(0, Math.min(x, maxX));
        y = Math.max(0, Math.min(y, maxY));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
