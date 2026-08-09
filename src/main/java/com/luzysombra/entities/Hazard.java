package com.luzysombra.entities;

import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Área de peligro. Subtipos:
 * <ul>
 *   <li>{@code penumbra} — daña SOLO a Luz.</li>
 *   <li>{@code luminosity} — daña SOLO a Sombra.</li>
 *   <li>{@code spikes} — daña a ambos personajes.</li>
 *   <li>{@code void} — vacío (agujero interno): ambos pierden una vida.</li>
 * </ul>
 */
public class Hazard extends GameObject {

    public enum HazardType {
        PENUMBRA, LUMINOSITY, SPIKES, VOID
    }

    private final HazardType hazardType;

    public Hazard(String id, String subtype, double x, double y, double width, double height) {
        super(id, "hazard", x, y, width, height);
        this.hazardType = parse(subtype);
        this.imagePath = switch (hazardType) {
            case PENUMBRA -> ResourcePaths.HAZARD_SHADOW;
            case LUMINOSITY -> ResourcePaths.HAZARD_LIGHT;
            case SPIKES -> ResourcePaths.SPIKES;
            case VOID -> null;
        };
    }

    private static HazardType parse(String subtype) {
        if (subtype == null) {
            return HazardType.SPIKES;
        }
        return switch (subtype.toLowerCase()) {
            case "penumbra" -> HazardType.PENUMBRA;
            case "luminosity", "luminosidad", "light" -> HazardType.LUMINOSITY;
            case "void", "abismo" -> HazardType.VOID;
            default -> HazardType.SPIKES;
        };
    }

    public HazardType getHazardType() {
        return hazardType;
    }

    /** ¿Daña a este jugador? (reglas de afinidad luz/sombra). */
    public boolean damages(Player player) {
        return switch (hazardType) {
            case PENUMBRA -> player instanceof LightPlayer;
            case LUMINOSITY -> player instanceof ShadowPlayer;
            case SPIKES, VOID -> true;
        };
    }

    @Override
    public Node createView() {
        StackPane pane = new StackPane();
        pane.setPrefSize(width, height);
        pane.setMouseTransparent(true);

        switch (hazardType) {
            case PENUMBRA -> buildPenumbra(pane);
            case LUMINOSITY -> buildLuminosity(pane);
            case SPIKES -> buildSpikes(pane);
            case VOID -> buildVoid(pane);
        }
        return pane;
    }

    private void buildPenumbra(StackPane pane) {
        if (!ResourceManager.getInstance().isMissing(imagePath)) {
            pane.getChildren().add(imageView());
        }
        Rectangle fog = new Rectangle(width, height);
        fog.setFill(Color.rgb(30, 24, 48, 0.42));
        fog.setStroke(Color.rgb(96, 72, 140, 0.7));
        fog.setStrokeWidth(2);
        pane.getChildren().add(fog);
        pulse(pane, 0.82, 1.0);
    }

    private void buildLuminosity(StackPane pane) {
        if (!ResourceManager.getInstance().isMissing(imagePath)) {
            pane.getChildren().add(imageView());
        }
        Rectangle glow = new Rectangle(width, height);
        glow.setFill(Color.rgb(255, 236, 180, 0.34));
        glow.setStroke(Color.rgb(220, 180, 90, 0.8));
        glow.setStrokeWidth(2);
        pane.getChildren().add(glow);
        pulse(pane, 0.86, 1.0);
    }

    private void buildSpikes(StackPane pane) {
        if (!ResourceManager.getInstance().isMissing(imagePath)) {
            pane.getChildren().add(imageView());
        }
        // Triángulos de emergencia si falta la imagen
        int count = Math.max(2, (int) (width / 26));
        double step = width / count;
        for (int i = 0; i < count; i++) {
            Polygon spike = new Polygon(
                    2 + i * step, height,
                    step / 2 + i * step, 4,
                    step - 2 + i * step, height);
            spike.setFill(Color.rgb(52, 40, 30));
            spike.setStroke(Color.rgb(140, 110, 80));
            spike.setStrokeWidth(1.5);
            pane.getChildren().add(spike);
        }
    }

    private void buildVoid(StackPane pane) {
        Rectangle empty = new Rectangle(width, height);
        empty.setFill(Color.rgb(12, 10, 18, 0.95));
        empty.setStroke(Color.rgb(60, 50, 80, 0.9));
        empty.setStrokeWidth(2);
        pane.getChildren().add(empty);
        pulse(pane, 0.8, 1.0);
    }

    private ImageView imageView() {
        ImageView image = new ImageView(ResourceManager.getInstance().getImage(imagePath));
        image.setPreserveRatio(false);
        image.setFitWidth(width);
        image.setFitHeight(height);
        image.setMouseTransparent(true);
        return image;
    }

    private void pulse(Node node, double min, double max) {
        FadeTransition ft = new FadeTransition(Duration.seconds(1.6), node);
        ft.setFromValue(min);
        ft.setToValue(max);
        ft.setInterpolator(Interpolator.EASE_BOTH);
        ft.setAutoReverse(true);
        ft.setCycleCount(FadeTransition.INDEFINITE);
        ft.play();
    }
}
