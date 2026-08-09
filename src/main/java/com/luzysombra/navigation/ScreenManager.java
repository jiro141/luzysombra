package com.luzysombra.navigation;

import com.luzysombra.util.AnimationUtils;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Gestor de pantallas y overlays dentro de UN SOLO Stage.
 * <p>
 * Estructura del contenedor raíz:
 * <pre>
 * StackPane root
 * ├── screenLayer      → contenido de la pantalla actual
 * ├── transitionLayer  → capa para transiciones (fundidos)
 * └── overlayLayer     → overlays internos (pausa, derrota, victoria)
 * </pre>
 * Los overlays oscurecen el fondo, bloquean los controles (ver InputManager)
 * y capturan el foco del teclado. Nunca se crea un Stage nuevo.
 */
public class ScreenManager {

    private final StackPane root = new StackPane();
    private final StackPane screenLayer = new StackPane();
    private final StackPane transitionLayer = new StackPane();
    private final StackPane overlayLayer = new StackPane();

    private ScreenType currentScreen = null;
    private Node currentContent = null;
    private Node currentOverlay = null;

    public ScreenManager() {
        root.getChildren().addAll(screenLayer, transitionLayer, overlayLayer);
        screenLayer.setMouseTransparent(false);
        transitionLayer.setMouseTransparent(true);
        // El overlayLayer NO usa pickOnBounds: un StackPane vacío con pickOnBounds=true
        // interceptaría TODOS los clics de la pantalla aunque no haya ningún overlay.
        // El bloqueo de los controles de detrás lo hace el propio overlay (su fondo
        // oscuro a pantalla completa cubre y captura los clics).
        overlayLayer.setMouseTransparent(false);
        overlayLayer.setPickOnBounds(false);
    }

    public StackPane getRoot() {
        return root;
    }

    // ---------------------------------------------------------------
    // Pantallas
    // ---------------------------------------------------------------

    /** Muestra una pantalla con una transición de fundido. */
    public void showScreen(ScreenType type, Node content) {
        this.currentScreen = type;
        if (currentContent == null) {
            currentContent = content;
            screenLayer.getChildren().setAll(content);
            content.setOpacity(0);
            AnimationUtils.fade(content, 1.0, Duration.millis(420)).play();
        } else {
            Node previous = currentContent;
            currentContent = content;
            content.setOpacity(0);
            screenLayer.getChildren().setAll(content);
            AnimationUtils.fade(content, 1.0, Duration.millis(420)).play();
            previous.setVisible(false);
        }
        requestScreenFocus();
    }

    public ScreenType getCurrentScreen() {
        return currentScreen;
    }

    public void requestScreenFocus() {
        if (currentContent != null) {
            currentContent.requestFocus();
        }
    }

    // ---------------------------------------------------------------
    // Overlays internos
    // ---------------------------------------------------------------

    /** Muestra un overlay (pausa, derrota, victoria) sobre la pantalla actual. */
    public void pushOverlay(Node overlay) {
        overlayLayer.getChildren().setAll(overlay);
        currentOverlay = overlay;
        overlayLayer.setVisible(true);
        overlay.setOpacity(0);
        AnimationUtils.fade(overlay, 1.0, Duration.millis(240)).play();
        overlay.requestFocus();
    }

    /** Oculta el overlay y devuelve el foco a la pantalla (para reanudar el juego). */
    public void popOverlay() {
        if (currentOverlay != null) {
            currentOverlay.setVisible(false);
            overlayLayer.getChildren().clear();
            overlayLayer.setVisible(false);
            currentOverlay = null;
            requestScreenFocus();
        }
    }

    public boolean hasOverlay() {
        return currentOverlay != null && overlayLayer.isVisible();
    }

    public void clearOverlays() {
        overlayLayer.getChildren().clear();
        overlayLayer.setVisible(false);
        currentOverlay = null;
    }
}
