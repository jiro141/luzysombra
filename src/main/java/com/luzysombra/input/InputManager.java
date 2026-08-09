package com.luzysombra.input;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Sistema de entrada basado en un Set de teclas activas.
 * <p>
 * El estado se mantiene entre frames: el game loop consulta {@link #isDown(KeyCode)}
 * para mover a ambos jugadores SIMULTÁNEAMENTE con varias teclas presionadas a la vez.
 * <p>
 * Además distingue pulsaciones de "borde" (el momento exacto en que una tecla se
 * presiona por primera vez) para acciones como ESC: una tecla mantenida no dispara
 * el borde repetidamente, evitando que la pausa se abra y cierre en bucle.
 */
public final class InputManager {

    /** Teclas actualmente presionadas (estado continuo). */
    private final Set<KeyCode> pressed = new HashSet<>();

    /** Teclas cuyo borde de pulsación ya fue consumido. */
    private final Set<KeyCode> edgeConsumed = new HashSet<>();

    /** Callbacks de borde: se invocan una única vez por pulsación física. */
    private Consumer<KeyCode> onEdgePress = null;
    private Consumer<KeyCode> onEdgeRelease = null;

    /** Si está deshabilitado, el juego ignora las teclas (overlays capturan el foco). */
    private boolean enabled = true;

    private final EventHandler<KeyEvent> pressHandler = this::handlePress;
    private final EventHandler<KeyEvent> releaseHandler = this::handleRelease;
    private Scene attachedScene = null;

    // ---------------------------------------------------------------
    // Ciclo de vida
    // ---------------------------------------------------------------

    public void attach(Scene scene) {
        if (attachedScene == scene) {
            return;
        }
        detach();
        this.attachedScene = scene;
        scene.addEventHandler(KeyEvent.KEY_PRESSED, pressHandler);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, releaseHandler);
    }

    public void detach() {
        if (attachedScene != null) {
            attachedScene.removeEventHandler(KeyEvent.KEY_PRESSED, pressHandler);
            attachedScene.removeEventHandler(KeyEvent.KEY_RELEASED, releaseHandler);
            attachedScene = null;
        }
    }

    private void handlePress(KeyEvent event) {
        if (!enabled) {
            return;
        }
        KeyCode code = event.getCode();
        if (pressed.add(code)) {
            // Borde de pulsación: solo la primera vez que se mantiene la tecla.
            edgeConsumed.remove(code);
            if (onEdgePress != null) {
                onEdgePress.accept(code);
            }
        }
        event.consume();
    }

    private void handleRelease(KeyEvent event) {
        if (!enabled) {
            return;
        }
        KeyCode code = event.getCode();
        if (pressed.remove(code)) {
            edgeConsumed.remove(code);
            if (onEdgeRelease != null) {
                onEdgeRelease.accept(code);
            }
        }
        event.consume();
    }

    // ---------------------------------------------------------------
    // Consultas para el game loop
    // ---------------------------------------------------------------

    /** Estado continuo: ¿la tecla está presionada en este frame? */
    public boolean isDown(KeyCode code) {
        return enabled && pressed.contains(code);
    }

    /**
     * Borde de pulsación: retorna true UNA sola vez por pulsación física.
     * Internamente usa la presencia en "pressed" como indicador de borde pendiente.
     */
    public boolean isEdgePressed(KeyCode code) {
        if (!enabled || !pressed.contains(code) || edgeConsumed.contains(code)) {
            return false;
        }
        edgeConsumed.add(code);
        return true;
    }

    // ---------------------------------------------------------------
    // Configuración
    // ---------------------------------------------------------------

    public void setOnEdgePress(Consumer<KeyCode> onEdgePress) {
        this.onEdgePress = onEdgePress;
    }

    public void setOnEdgeRelease(Consumer<KeyCode> onEdgeRelease) {
        this.onEdgeRelease = onEdgeRelease;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            clearAll();
        }
    }

    /** Limpia el estado: evita teclas "pegadas" al reanudar el juego. */
    public void clearAll() {
        pressed.clear();
        edgeConsumed.clear();
    }
}
