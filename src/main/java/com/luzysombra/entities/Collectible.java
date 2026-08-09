package com.luzysombra.entities;

import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Coleccionable del nivel.
 * <ul>
 *   <li>{@code albor} — llama de luz blanca, exclusivo de Luz.</li>
 *   <li>{@code obsidian} — cristal negro/violeta, exclusivo de Sombra.</li>
 * </ul>
 */
public class Collectible extends GameObject {

    public enum CollectibleType {
        ALBOR, OBSIDIAN
    }

    private final CollectibleType collectibleType;
    private boolean collected;

    public Collectible(String id, String subtype, double x, double y) {
        super(id, "collectible", x, y, 0, 0);
        this.collectibleType = parse(subtype);
        this.width = 34;
        this.height = 34;
        this.x = x - width / 2.0;
        this.y = y - height / 2.0;
        this.imagePath = (collectibleType == CollectibleType.ALBOR)
                ? ResourcePaths.ALBOR
                : ResourcePaths.OBSIDIAN;
    }

    private static CollectibleType parse(String subtype) {
        return (subtype != null && subtype.equalsIgnoreCase("obsidian"))
                ? CollectibleType.OBSIDIAN
                : CollectibleType.ALBOR;
    }

    public CollectibleType getCollectibleType() {
        return collectibleType;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        this.collected = true;
    }

    /** ¿Puede este jugador recogerlo? (cada personaje solo su propio coleccionable). */
    public boolean canCollect(Player player) {
        boolean isLight = player instanceof LightPlayer;
        return isLight == (collectibleType == CollectibleType.ALBOR);
    }

    @Override
    public Node createView() {
        StackPane pane = new StackPane();
        pane.setPrefSize(width, height);
        pane.setMouseTransparent(true);

        if (!ResourceManager.getInstance().isMissing(imagePath)) {
            ImageView image = new ImageView(ResourceManager.getInstance().getImage(imagePath));
            image.setFitWidth(width);
            image.setFitHeight(height);
            image.setPreserveRatio(true);
            pane.getChildren().add(image);
            // Flotación
            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                    Duration.seconds(1.8), pane);
            tt.setToY(-8);
            tt.setInterpolator(Interpolator.EASE_BOTH);
            tt.setAutoReverse(true);
            tt.setCycleCount(javafx.animation.TranslateTransition.INDEFINITE);
            tt.play();
        } else {
            // Versión vectorial de emergencia
            if (collectibleType == CollectibleType.ALBOR) {
                buildAlborVector(pane);
            } else {
                buildObsidianVector(pane);
            }
        }

        DropShadow glow = new DropShadow(16,
                collectibleType == CollectibleType.ALBOR
                        ? Color.rgb(255, 250, 220, 0.9)
                        : Color.rgb(140, 90, 220, 0.9));
        pane.setEffect(glow);

        // Pulso de escala suave
        ScaleTransition st = new ScaleTransition(Duration.seconds(1.4), pane);
        st.setFromX(0.92);
        st.setFromY(0.92);
        st.setToX(1.08);
        st.setToY(1.08);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.setAutoReverse(true);
        st.setCycleCount(ScaleTransition.INDEFINITE);
        st.play();
        return pane;
    }

    private void buildAlborVector(StackPane pane) {
        Polygon flame = new Polygon(
                17, 2,
                27, 18,
                24, 28,
                10, 28,
                7, 18);
        flame.setFill(Color.rgb(255, 252, 235));
        flame.setStroke(Color.rgb(255, 220, 150));
        pane.getChildren().add(flame);
    }

    private void buildObsidianVector(StackPane pane) {
        Polygon crystal = new Polygon(
                17, 2,
                28, 14,
                23, 30,
                11, 30,
                6, 14);
        crystal.setFill(Color.rgb(24, 18, 40));
        crystal.setStroke(Color.rgb(150, 100, 230));
        crystal.setStrokeWidth(2);
        pane.getChildren().add(crystal);
        Rectangle facet = new Rectangle(6, 8);
        facet.setFill(Color.rgb(150, 100, 230, 0.5));
        facet.setRotate(30);
        facet.setTranslateX(1);
        facet.setTranslateY(2);
        pane.getChildren().add(facet);
    }
}
