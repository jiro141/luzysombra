package com.luzysombra.entities;

import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Puerta de salida de cada personaje.
 * <ul>
 *   <li>{@code light} — puerta blanca/dorada, destino de Luz.</li>
 *   <li>{@code shadow} — puerta negra/violeta, destino de Sombra.</li>
 * </ul>
 * La puerta permanece cerrada (bloqueada) hasta que el personaje correspondiente
 * recoge TODOS sus coleccionables; entonces se ilumina y muestra un indicador.
 */
public class Door extends GameObject {

    private final String subtype; // "light" | "shadow"
    private boolean open;
    private ImageView closedView;
    private ImageView openView;
    private Node indicator;

    public Door(String id, String subtype, double x, double y, double width, double height) {
        super(id, "door", x, y, width, height);
        this.subtype = subtype;
        this.open = false;
        this.imagePath = "light".equals(subtype)
                ? ResourcePaths.DOOR_LIGHT_CLOSED
                : ResourcePaths.DOOR_SHADOW_CLOSED;
    }

    public boolean isOpen() {
        return open;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setOpen(boolean open) {
        this.open = open;
        updateVisual();
    }

    /** La puerta de Luz solo acepta a Luz, y la de Sombra solo a Sombra. */
    public boolean accepts(Player player) {
        boolean isLight = player instanceof LightPlayer;
        return isLight == "light".equals(subtype);
    }

    /** Sincroniza el aspecto visual según el estado abierto/cerrado. */
    public void updateVisual() {
        if (closedView == null) {
            return;
        }
        closedView.setVisible(!open);
        openView.setVisible(open);
        if (indicator != null) {
            indicator.setVisible(open);
        }
    }

    @Override
    public Node createView() {
        StackPane pane = new StackPane();
        pane.setPrefSize(width, height);
        pane.setMouseTransparent(true);

        String closedPath = "light".equals(subtype)
                ? ResourcePaths.DOOR_LIGHT_CLOSED
                : ResourcePaths.DOOR_SHADOW_CLOSED;
        String openPath = "light".equals(subtype)
                ? ResourcePaths.DOOR_LIGHT_OPEN
                : ResourcePaths.DOOR_SHADOW_OPEN;

        closedView = imageView(closedPath);
        openView = imageView(openPath);
        pane.getChildren().addAll(closedView, openView);

        if (ResourceManager.getInstance().isMissing(closedPath)) {
            // Arco vectorial de emergencia
            Arc arch = new Arc(width / 2.0, height - 10, width / 2.0 - 8, height - 20, 180, 180);
            arch.setType(ArcType.OPEN);
            arch.setFill(null);
            arch.setStroke(Color.rgb(90, 70, 50));
            arch.setStrokeWidth(6);
            Rectangle pillar = new Rectangle(width - 30, height);
            pillar.setFill(Color.rgb(66, 50, 36));
            pane.getChildren().add(1, pillar);
            pane.getChildren().add(2, arch);
        }

        // Indicador: flecha pulsante cuando la puerta está abierta
        indicator = createIndicator();
        indicator.setVisible(false);
        StackPane.setAlignment(indicator, javafx.geometry.Pos.BOTTOM_CENTER);
        pane.getChildren().add(indicator);

        return pane;
    }

    private ImageView imageView(String path) {
        ImageView image = new ImageView(ResourceManager.getInstance().getImage(path));
        image.setPreserveRatio(false);
        image.setFitWidth(width);
        image.setFitHeight(height);
        image.setMouseTransparent(true);
        return image;
    }

    private Node createIndicator() {
        StackPane box = new StackPane();
        Polygon arrow = new Polygon(0, -8, 14, -8, 7, 2);
        arrow.setFill(Color.rgb(255, 250, 220, 0.95));
        DropShadow glow = new DropShadow(12, Color.rgb(255, 240, 180, 0.9));
        arrow.setEffect(glow);
        box.getChildren().add(arrow);
        box.setTranslateY(18);

        FadeTransition ft = new FadeTransition(Duration.millis(700), box);
        ft.setFromValue(0.25);
        ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_BOTH);
        ft.setAutoReverse(true);
        ft.setCycleCount(FadeTransition.INDEFINITE);
        ft.play();
        return box;
    }
}
