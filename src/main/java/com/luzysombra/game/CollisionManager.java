package com.luzysombra.game;

import com.luzysombra.entities.GameObject;
import com.luzysombra.entities.Ladder;
import com.luzysombra.entities.MovingPlatform;
import com.luzysombra.entities.Player;
import com.luzysombra.util.CollisionUtils;

/**
 * Resolución de colisiones del motor.
 * <p>
 * Movimiento por ejes: primero el eje X (y se empuja contra los sólidos), luego
 * el eje Y (aterrizaje / techo). Además gestiona escaleras y el arrastre de las
 * plataformas móviles. Los eventos (peligros, coleccionables, puertas, puntos
 * seguros) los detecta el GameLoop con las consultas de colisión.
 */
public final class CollisionManager {

    private CollisionManager() {
    }

    /**
     * Integra la velocidad del jugador, corrige colisiones contra sólidos y
     * detecta escaleras y plataformas móviles para el arrastre.
     */
    public static void resolve(Player player, Level level, double dt) {
        boolean wasGrounded = player.isGrounded();
        player.setGrounded(false);
        player.setOnLadder(false);

        // ------------------------------------------------------------
        // Eje X
        // ------------------------------------------------------------
        player.setX(player.getX() + player.getVx() * dt);
        for (GameObject solid : level.getSolids()) {
            if (intersects(player, solid)) {
                double overlap = CollisionUtils.overlapX(
                        player.getX(), player.getWidth(),
                        solid.getX(), solid.getWidth());
                player.setX(player.getX() + overlap);
                player.setVx(0);
            }
        }
        clampX(player, level);

        // ------------------------------------------------------------
        // Eje Y
        // ------------------------------------------------------------
        player.setY(player.getY() + player.getVy() * dt);
        for (GameObject solid : level.getSolids()) {
            if (intersects(player, solid)) {
                if (player.getVy() > 0) {
                    // Aterrizaje sobre la superficie
                    player.setY(solid.getY() - player.getHeight());
                    player.setGrounded(true);
                    player.setVy(0);
                } else if (player.getVy() < 0) {
                    // Golpe contra el techo
                    player.setY(solid.getBottom());
                    player.setVy(0);
                } else {
                    // Sin velocidad vertical: empuje mínimo hacia arriba
                    player.setY(solid.getY() - player.getHeight());
                    player.setGrounded(true);
                }
            }
        }

        if (player.getY() < 0) {
            player.setY(0);
            player.setVy(0);
        }

        // ------------------------------------------------------------
        // Escaleras: anulan la gravedad mientras el jugador está dentro
        // ------------------------------------------------------------
        for (Ladder ladder : level.getLadders()) {
            if (intersects(player, ladder)) {
                player.setOnLadder(true);
                break;
            }
        }

        // ------------------------------------------------------------
        // Arrastre de plataformas móviles
        // ------------------------------------------------------------
        for (MovingPlatform mp : level.getMovingPlatforms()) {
            boolean onTop = CollisionUtils.intersects(
                    player.getX(), player.getY() + player.getHeight() - 4, player.getWidth(), 6,
                    mp.getX(), mp.getY(), mp.getWidth(), mp.getHeight());
            if (onTop || (player.isGrounded() && Math.abs(player.getBottom() - mp.getY()) < 14
                    && CollisionUtils.intersects(player.getX(), player.getY(), player.getWidth(), player.getHeight(),
                    mp.getX(), mp.getY(), mp.getWidth(), mp.getHeight()))) {
                player.setX(player.getX() + mp.getDeltaX());
                player.setY(player.getY() + mp.getDeltaY());
                if (Math.abs(mp.getDeltaY()) > 0 && mp.getDeltaY() > 0) {
                    // La plataforma baja: el jugador viaja con ella
                }
                player.syncView();
            }
        }

        player.syncView();
    }

    private static void clampX(Player player, Level level) {
        if (player.getX() < 0) {
            player.setX(0);
            player.setVx(0);
        } else if (player.getRight() > level.getWidth()) {
            player.setX(level.getWidth() - player.getWidth());
            player.setVx(0);
        }
    }

    private static boolean intersects(Player p, GameObject o) {
        return CollisionUtils.intersects(
                p.getX(), p.getY(), p.getWidth(), p.getHeight(),
                o.getX(), o.getY(), o.getWidth(), o.getHeight());
    }
}
