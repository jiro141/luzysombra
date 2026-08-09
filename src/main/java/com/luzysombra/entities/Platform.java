package com.luzysombra.entities;

import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Plataforma sólida estática (suelo, plataformas suspendidas, paredes de apoyo).
 */
public class Platform extends GameObject {

    public Platform(String id, double x, double y, double width, double height) {
        super(id, "platform", x, y, width, height);
        this.imagePath = ResourcePaths.PLATFORM;
    }

    @Override
    public Node createView() {
        StackPane pane = new StackPane();
        pane.setPrefSize(width, height);

        ImageView image = new ImageView(ResourceManager.getInstance().getImage(imagePath));
        image.setPreserveRatio(false);
        image.setFitWidth(width);
        image.setFitHeight(height);
        image.setMouseTransparent(true);
        pane.getChildren().add(image);

        // Rejilla de seguridad: si la imagen es un placeholder se nota, pero igualmente
        // se superpone un borde sutil que define el sólido de forma visual.
        Rectangle border = new Rectangle(width, height);
        border.setFill(null);
        border.setStroke(Color.rgb(24, 16, 10, 0.55));
        border.setStrokeWidth(2);
        border.setMouseTransparent(true);
        pane.getChildren().add(border);

        DropShadow shadow = new DropShadow(10, Color.rgb(0, 0, 0, 0.45));
        pane.setEffect(shadow);
        return pane;
    }
}
