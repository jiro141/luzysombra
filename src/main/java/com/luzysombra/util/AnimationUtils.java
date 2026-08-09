package com.luzysombra.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utilidades reutilizables para animaciones de interfaz y entrada/salida de pantallas.
 * Todas las animaciones se ejecutan sobre nodos JavaFX existentes (no se crean ventanas).
 */
public final class AnimationUtils {

    private AnimationUtils() {
    }

    /** Desvanece el nodo hasta la opacidad indicada. */
    public static FadeTransition fade(Node node, double to, Duration duration) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setToValue(to);
        ft.setInterpolator(Interpolator.EASE_BOTH);
        return ft;
    }

    /** Desplaza verticalmente el nodo (para entradas suaves). */
    public static TranslateTransition slide(Node node, double deltaY, Duration duration) {
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setByY(deltaY);
        tt.setInterpolator(Interpolator.EASE_OUT);
        return tt;
    }

    /** Aplica una transición de entrada: aparece y se desliza ligeramente hacia arriba. */
    public static void entrance(Node node, Duration duration) {
        node.setOpacity(0.0);
        node.setTranslateY(24.0);
        node.setVisible(true);
        FadeTransition ft = fade(node, 1.0, duration);
        TranslateTransition tt = slide(node, -24.0, duration);
        tt.play();
        ft.play();
    }

    /** Aplica una transición de salida y oculta el nodo al terminar. */
    public static void exit(Node node, Duration duration, Runnable onFinished) {
        FadeTransition ft = fade(node, 0.0, duration);
        ft.setOnFinished(e -> {
            node.setVisible(false);
            if (onFinished != null) {
                onFinished.run();
            }
        });
        ft.play();
    }

    /** Parpadeo rápido de opacidad, útil para avisos visuales. */
    public static void blink(Node node, int times, Duration each) {
        FadeTransition ft = new FadeTransition(each.multiply(times), node);
        ft.setFromValue(1.0);
        ft.setToValue(0.25);
        ft.setAutoReverse(true);
        ft.setCycleCount(times * 2);
        ft.play();
    }
}
