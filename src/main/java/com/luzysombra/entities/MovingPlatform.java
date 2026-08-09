package com.luzysombra.entities;

import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Plataforma móvil: se desplaza de forma sinusoidal entre dos extremos
 * (base ± rango/2) en un eje horizontal o vertical.
 * <p>
 * Si un jugador está parado sobre ella, el motor lo arrastra con el movimiento
 * de la plataforma (ver CollisionManager).
 */
public class MovingPlatform extends GameObject {

    private final String axis;      // "horizontal" | "vertical"
    private final double range;     // amplitud total del recorrido en píxeles
    private final double speed;     // píxeles por segundo
    private final double baseX;
    private final double baseY;
    private double prevX;
    private double prevY;
    private boolean active = true;
    private String switchId;

    private Circle gemView;

    public MovingPlatform(String id, double x, double y, double width, double height,
                          String axis, double range, double speed) {
        super(id, "moving_platform", x, y, width, height);
        this.axis = axis;
        this.range = Math.max(1, range);
        this.speed = Math.max(1, speed);
        this.baseX = x;
        this.baseY = y;
        this.prevX = x;
        this.prevY = y;
        this.imagePath = ResourcePaths.MOVING_PLATFORM;
    }

    /** Actualiza la posición según el tiempo transcurrido de nivel.
     *  Las plataformas controladas por un interruptor solo se mueven mientras
     *  el interruptor está presionado (active == true). */
    public void update(double levelTime) {
        if (switchId != null && !active) {
            return; // congelada: esperando a que se presione el interruptor
        }
        prevX = x;
        prevY = y;
        double phase = (levelTime * speed) / range;
        double offset = Math.sin(phase) * (range / 2.0);
        if ("vertical".equals(axis)) {
            y = baseY + offset;
        } else {
            x = baseX + offset;
        }
        syncView();
        // La gemita brilla al pasar por el centro
        if (gemView != null) {
            double centerProximity = 1.0 - Math.abs(offset) / (range / 2.0);
            gemView.setOpacity(0.55 + 0.45 * centerProximity);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /** Asigna el id del interruptor que controla esta plataforma (null = siempre activa). */
    public void setSwitchId(String switchId) {
        this.switchId = switchId;
    }

    public double getDeltaX() {
        return x - prevX;
    }

    public double getDeltaY() {
        return y - prevY;
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

        gemView = new Circle(7);
        gemView.setFill(Color.rgb(231, 200, 130));
        gemView.setStroke(Color.rgb(90, 60, 20));
        gemView.setMouseTransparent(true);
        gemView.setTranslateX(width / 2.0 - 12);
        StackPane.setAlignment(gemView, javafx.geometry.Pos.CENTER_RIGHT);
        pane.getChildren().add(gemView);

        Rectangle border = new Rectangle(width, height);
        border.setFill(null);
        border.setStroke(Color.rgb(24, 16, 10, 0.5));
        border.setStrokeWidth(2);
        border.setMouseTransparent(true);
        pane.getChildren().add(border);

        DropShadow shadow = new DropShadow(10, Color.rgb(0, 0, 0, 0.4));
        pane.setEffect(shadow);
        return pane;
    }
}
