package com.luzysombra.entities;

import com.luzysombra.resources.ResourceManager;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Punto seguro de reaparición: al tocarlo, el personaje actualiza su respawn.
 * Se representa como un pedestal con una runa que se ilumina al activarse.
 */
public class Checkpoint extends GameObject {

    private boolean activated;
    private Circle rune;

    public Checkpoint(String id, double x, double y) {
        super(id, "checkpoint", x, y, 44, 46);
    }

    public boolean isActivated() {
        return activated;
    }

    public void activate() {
        this.activated = true;
        if (rune != null) {
            rune.setFill(Color.rgb(255, 236, 170));
        }
    }

    @Override
    public Node createView() {
        StackPane pane = new StackPane();
        pane.setPrefSize(width, height);
        pane.setMouseTransparent(true);

        Rectangle pedestal = new Rectangle(width - 10, 12);
        pedestal.setArcWidth(6);
        pedestal.setArcHeight(6);
        pedestal.setFill(Color.rgb(96, 74, 52));
        pedestal.setStroke(Color.rgb(200, 170, 120));
        pedestal.setTranslateY(15);
        pane.getChildren().add(pedestal);

        rune = new Circle(8);
        rune.setFill(Color.rgb(120, 96, 70));
        DropShadow glow = new DropShadow(10, Color.rgb(255, 230, 160, 0.9));
        rune.setEffect(glow);
        rune.setTranslateY(-8);
        pane.getChildren().add(rune);

        return pane;
    }
}
