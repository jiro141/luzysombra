package com.luzysombra.ui;

import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import javafx.animation.ScaleTransition;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Set;

/**
 * Botón del juego con estilo visual consistente (ver game.css), estados
 * normal/hover/presionado/deshabilitado, cursor de mano y sonido de clic.
 * <p>
 * La acción se dispara en {@code MOUSE_RELEASED} solo si el cursor sigue sobre
 * el botón (usando {@code mousePressed}/{@code mouseReleased} en lugar del
 * {@code ActionEvent} interno): esto garantiza que el primer clic funcione
 * aunque la ventana no tuviera foco, y evita la doble ejecución.
 */
public class GameButton extends Button {

    public enum Variant {
        PRIMARY, GHOST, DANGER
    }

    private static final double PRESS_SCALE = 0.97;

    private final ScaleTransition resetTransition = new ScaleTransition(Duration.millis(110), this);
    private final Set<KeyCode> keyboardPressed = new HashSet<>();
    private boolean mouseDown = false;

    public GameButton(String text, Variant variant, Runnable action) {
        super(text);
        getStyleClass().add("game-button");
        getStyleClass().add(variantStyle(variant));
        setFocusTraversable(false);
        setPickOnBounds(true);
        setCursor(Cursor.HAND);

        resetTransition.setToX(1.0);
        resetTransition.setToY(1.0);

        // Hover: solo cursor de mano (la iluminación dorada la aplica CSS :hover)
        setOnMouseEntered(e -> setCursor(Cursor.HAND));
        setOnMouseExited(e -> {
            setCursor(Cursor.DEFAULT);
            mouseDown = false;
            restoreScale();
        });

        // Pulsación: efecto sutil y marca para disparar la acción al soltar.
        setOnMousePressed(e -> {
            if (isDisabled()) {
                return;
            }
            mouseDown = true;
            setScaleX(PRESS_SCALE);
            setScaleY(PRESS_SCALE);
        });

        // Disparo único: al soltar, SOLO si el mouse sigue sobre el botón.
        setOnMouseReleased(e -> {
            boolean fire = mouseDown && isHover() && !isDisabled();
            mouseDown = false;
            restoreScale();
            if (fire && action != null) {
                ResourceManager.getInstance().playSound(ResourcePaths.SOUND_CLICK);
                action.run();
            }
        });

        // Navegación con teclado (por si el botón recibe foco, p. ej. en overlays).
        // El Set evita la doble ejecución por la repetición de teclas del sistema.
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                e.consume();
                if (keyboardPressed.add(e.getCode()) && !isDisabled() && action != null) {
                    ResourceManager.getInstance().playSound(ResourcePaths.SOUND_CLICK);
                    action.run();
                }
            }
        });
        setOnKeyReleased(e -> keyboardPressed.remove(e.getCode()));
    }

    public GameButton(String text, Runnable action) {
        this(text, Variant.PRIMARY, action);
    }

    private void restoreScale() {
        if (getScaleX() != 1.0 || getScaleY() != 1.0) {
            resetTransition.playFromStart();
        }
    }

    private static String variantStyle(Variant variant) {
        return switch (variant) {
            case PRIMARY -> "game-button-primary";
            case GHOST -> "game-button-ghost";
            case DANGER -> "game-button-danger";
        };
    }
}
