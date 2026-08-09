package com.luzysombra.entities;

import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Interruptor de presión cooperativo: cuando el personaje indicado (o ambos)
 * se para sobre él, activa su plataforma móvil objetivo.
 * <ul>
 *   <li>{@code operator = "light"} → solo Luz lo activa.</li>
 *   <li>{@code operator = "shadow"} → solo Sombra lo activa.</li>
 *   <li>{@code operator = "any"} → cualquiera lo activa.</li>
 * </ul>
 * La plataforma móvil con {@code switch = "id-del-switch"} solo se mueve
 * mientras su interruptor está presionado.
 */
public class Switch extends GameObject {

    private final String operator;
    private final String targetId;
    private boolean pressed;
    private Circle rune;

    public Switch(String id, String operator, String targetId, double x, double y, double width, double height) {
        super(id, "switch", x, y, width, height);
        this.operator = operator == null ? "any" : operator;
        this.targetId = targetId;
    }

    public String getTargetId() {
        return targetId;
    }

    /** ¿Lo activa este jugador según el operador configurado? */
    public boolean isPressedBy(Player player) {
        boolean isLight = player instanceof LightPlayer;
        return switch (operator) {
            case "light" -> isLight;
            case "shadow" -> !isLight;
            default -> true; // any
        };
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
        if (rune != null) {
            rune.setFill(pressed ? Color.rgb(255, 236, 170) : Color.rgb(90, 70, 50));
        }
    }

    @Override
    public Node createView() {
        StackPane pane = new StackPane();
        pane.setPrefSize(width, height);
        pane.setMouseTransparent(true);

        Rectangle base = new Rectangle(width, height);
        base.setArcWidth(8);
        base.setArcHeight(8);
        base.setFill(Color.rgb(66, 50, 36));
        base.setStroke(Color.rgb(184, 155, 94, 0.7));
        base.setStrokeWidth(2);
        pane.getChildren().add(base);

        rune = new Circle(Math.min(width, height) / 2.0 - 6);
        rune.setFill(Color.rgb(90, 70, 50));
        DropShadow glow = new DropShadow(12, Color.rgb(255, 230, 160, 0.9));
        rune.setEffect(glow);
        pane.getChildren().add(rune);

        return pane;
    }
}
