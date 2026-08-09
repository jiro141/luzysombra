package com.luzysombra.entities;

import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

/**
 * Escalera: dentro de su área el jugador puede subir y bajar con las teclas
 * verticales, anulando la gravedad y el salto normal.
 */
public class Ladder extends GameObject {

    public Ladder(String id, double x, double y, double width, double height) {
        super(id, "ladder", x, y, width, height);
        this.imagePath = ResourcePaths.LADDER;
    }

    @Override
    public Node createView() {
        StackPane pane = new StackPane();
        pane.setPrefSize(width, height);

        if (!ResourceManager.getInstance().isMissing(imagePath)) {
            ImageView image = new ImageView(ResourceManager.getInstance().getImage(imagePath));
            image.setPreserveRatio(false);
            image.setFitWidth(width);
            image.setFitHeight(height);
            image.setMouseTransparent(true);
            pane.getChildren().add(image);
        } else {
            // Versión vectorial de emergencia: barrotes de madera oscura
            List<Node> bars = new ArrayList<>();
            for (int i = 0; i <= 6; i++) {
                Line vertical = new Line(8 + i * 14, 2, 8 + i * 14, height - 2);
                vertical.setStroke(Color.rgb(74, 56, 40));
                vertical.setStrokeWidth(3);
                bars.add(vertical);
            }
            for (int j = 0; j <= 8; j++) {
                double fy = 4 + j * (height - 8) / 8.0;
                Line horizontal = new Line(2, fy, width - 2, fy);
                horizontal.setStroke(Color.rgb(122, 92, 64));
                horizontal.setStrokeWidth(5);
                bars.add(horizontal);
            }
            pane.getChildren().addAll(bars);
        }
        return pane;
    }
}
