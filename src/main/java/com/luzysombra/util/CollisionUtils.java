package com.luzysombra.util;

/**
 * Utilidades de colisiones AABB (rectángulos alineados a los ejes).
 * Todas las entidades del juego trabajan con rectángulos en coordenadas del mundo.
 */
public final class CollisionUtils {

    private CollisionUtils() {
    }

    /** Comprueba si dos rectángulos se superponen. */
    public static boolean intersects(
            double ax, double ay, double aw, double ah,
            double bx, double by, double bw, double bh) {
        return ax < bx + bw
                && ax + aw > bx
                && ay < by + bh
                && ay + ah > by;
    }

    /** Comprueba si el punto está dentro del rectángulo. */
    public static boolean contains(double px, double py, double rx, double ry, double rw, double rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }

    /** Distancia de penetración horizontal mínima cuando hay intersección (positiva = empujar hacia la derecha). */
    public static double overlapX(double ax, double aw, double bx, double bw) {
        double rightOverlap = (ax + aw) - bx;
        double leftOverlap = (bx + bw) - ax;
        return (rightOverlap < leftOverlap) ? -rightOverlap : leftOverlap;
    }

    /** Distancia de penetración vertical mínima cuando hay intersección (positiva = empujar hacia abajo). */
    public static double overlapY(double ay, double ah, double by, double bh) {
        double bottomOverlap = (ay + ah) - by;
        double topOverlap = (by + bh) - ay;
        return (bottomOverlap < topOverlap) ? -bottomOverlap : topOverlap;
    }
}
