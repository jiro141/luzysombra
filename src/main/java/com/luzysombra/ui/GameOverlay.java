package com.luzysombra.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Base de los overlays internos (pausa, derrota, victoria).
 * <p>
 * Oscurece el fondo manteniendo visible el escenario, bloquea los controles
 * (el GameScreen deshabilita el InputManager) y captura el foco del teclado.
 * Desaparece al reanudar o navegar. Todo ocurre dentro del StackPane raíz.
 */
public abstract class GameOverlay extends StackPane {

    protected final VBox panel = new VBox(18);

    protected GameOverlay() {
        setPrefSize(1600, 900);
        setPickOnBounds(true);
        // El overlay recibe foco de teclado (ESC en pausa) y bloquea los clics:
        // el fondo oscuro cubre toda la pantalla y se consume cualquier clic que
        // no caiga sobre un control del propio overlay.
        setFocusTraversable(true);
        setOnMousePressed(e -> e.consume());

        // Fondo oscurecido con viñeta
        Rectangle dim = new Rectangle(1600, 900);
        dim.setFill(Color.rgb(8, 6, 12, 0.78));
        getChildren().add(dim);

        // Panel central
        panel.setMaxWidth(640);
        panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().add("overlay-panel");
        panel.setPadding(new Insets(36, 44, 36, 44));
        getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.CENTER);
    }

    protected Label title(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }

    protected void add(Node... nodes) {
        panel.getChildren().addAll(nodes);
    }
}
