package com.luzysombra.entities;

import com.luzysombra.config.GameConfig;
import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista animada de un personaje.
 * <p>
 * Si las imágenes PNG existen en resources, se usan los frames de animación
 * (idle / run-1 / run-2 / jump). Si faltan, se dibuja una silueta vectorial
 * procedural original (Luz: espíritu de luz; Sombra: criatura de la penumbra).
 */
public final class PlayerView {

    private final boolean isLight;
    private final StackPane root = new StackPane();
    private final List<Circle> smokeParticles = new ArrayList<>();

    private ImageView[] frames; // no nulo si se usan imágenes
    private Group vectorBody;
    private Rectangle hurtFlash;
    private DropShadow aura;
    private double animTime;

    private static final int IDLE = 0;
    private static final int WALK = 1;
    private static final int RUN = 2;
    private static final int JUMP = 3;
    private static final int FALL = 4;

    private static final String[] LIGHT_FRAMES = {
            ResourcePaths.LIGHT_IDLE,
            ResourcePaths.LIGHT_WALK,
            ResourcePaths.LIGHT_RUN,
            ResourcePaths.LIGHT_JUMP,
            ResourcePaths.LIGHT_FALL
    };

    private static final String[] SHADOW_FRAMES = {
            ResourcePaths.SHADOW_IDLE,
            ResourcePaths.SHADOW_WALK,
            ResourcePaths.SHADOW_RUN,
            ResourcePaths.SHADOW_JUMP,
            ResourcePaths.SHADOW_FALL
    };

    public PlayerView(boolean isLight) {
        this.isLight = isLight;
        root.setPrefSize(46, 72);
        root.setMouseTransparent(true);

        String[] paths = isLight ? LIGHT_FRAMES : SHADOW_FRAMES;
        boolean imagesAvailable = !ResourceManager.getInstance().isMissingScaled(paths[IDLE], 96, 128);

        if (imagesAvailable) {
            frames = new ImageView[paths.length];
            for (int i = 0; i < paths.length; i++) {
                // Se cargan escaladas para no inflar el heap (las fuentes son 1414x2000).
                frames[i] = new ImageView(ResourceManager.getInstance().getImage(paths[i], 96, 128));
                frames[i].setFitWidth(64);
                frames[i].setFitHeight(72);
                frames[i].setPreserveRatio(true);
                frames[i].setVisible(i == IDLE);
                root.getChildren().add(frames[i]);
            }
        } else {
            vectorBody = isLight ? buildLightVector() : buildShadowVector();
            root.getChildren().add(vectorBody);
        }

        aura = new DropShadow(26, isLight ? Color.rgb(255, 214, 140, 0.9) : Color.rgb(120, 70, 200, 0.9));
        root.setEffect(aura);

        hurtFlash = new Rectangle(46, 72);
        hurtFlash.setFill(isLight ? Color.rgb(255, 120, 90, 0.55) : Color.rgb(180, 60, 160, 0.55));
        hurtFlash.setVisible(false);
        root.getChildren().add(hurtFlash);

        if (!isLight) {
            initSmoke();
        }
    }

    public Node getNode() {
        return root;
    }

    // ================================================================
    // Actualización por frame
    // ================================================================

public void update(double delta, boolean moving, boolean airborne, double vx, double vy,
                   boolean onLadder, boolean hurt, double invulnerableTimer, int facing) {
        animTime += delta;

        // Orientación
        root.setScaleX(facing);

        // Parpadeo de invulnerabilidad (~12 Hz)
        if (invulnerableTimer > 0) {
            root.setOpacity((Math.sin(animTime * 38.0) > 0) ? 1.0 : 0.3);
        } else {
            root.setOpacity(1.0);
        }

        // Flash de impacto
        hurtFlash.setVisible(hurt);
        if (hurt) {
            hurtFlash.setOpacity(0.5 + 0.5 * Math.sin(animTime * 30.0));
        }

        // Animación de movimiento / salto
        double bounce = moving && !airborne ? Math.abs(Math.sin(animTime * 10.0)) * 0.06 : 0.0;
        double stretch = airborne ? 0.06 : 0.0;
        if (frames != null) {
            int frame = selectFrame(moving, airborne, vx, vy, onLadder);
            for (int i = 0; i < frames.length; i++) {
                frames[i].setVisible(i == frame);
            }
            root.setScaleY(1.0 + stretch);
        } else {
            vectorBody.setScaleY(1.0 + bounce + stretch);
            animateVector(moving, airborne, onLadder);
        }

        if (aura != null) {
            aura.setRadius(22 + 6 * Math.sin(animTime * 2.2));
        }
        animateSmoke(delta, airborne);
    }

    /**
     * Selecciona el frame según el estado del personaje:
     * en el aire = saltar (subiendo, vy<0) o caer (bajando, vy>=0);
     * en el suelo = caminar (|vx| bajo) o correr (|vx| alto).
     */
    private int selectFrame(boolean moving, boolean airborne, double vx, double vy, boolean onLadder) {
        if (airborne) {
            return vy < 0 ? JUMP : FALL;
        }
        if (!moving || onLadder) {
            return IDLE;
        }
        return Math.abs(vx) >= GameConfig.WALK_SPEED ? RUN : WALK;
    }

    private void animateVector(boolean moving, boolean airborne, boolean onLadder) {
        // El velo de la luz ondea; la sombra respira
        double wave = moving || airborne ? Math.sin(animTime * 12.0) * 2.2 : Math.sin(animTime * 2.0) * 1.0;
        if (isLight) {
            Node veil = vectorBody.getChildren().get(2);
            if (veil instanceof Polygon poly) {
                poly.getPoints().set(1, 20.0 + wave);
                poly.getPoints().set(3, 18.0 + wave);
            }
        }
    }

    private void animateSmoke(double delta, boolean airborne) {
        if (isLight) {
            return;
        }
        for (int i = 0; i < smokeParticles.size(); i++) {
            Circle c = smokeParticles.get(i);
            c.setTranslateY(c.getTranslateY() - delta * (18 + i * 3));
            c.setTranslateX(c.getTranslateX() + Math.sin(animTime * 1.7 + i) * 0.35);
            c.setOpacity(Math.max(0.05, c.getOpacity() - delta * 0.6));
            if (c.getOpacity() <= 0.05) {
                c.setTranslateY(8 + (i % 3) * 14);
                c.setOpacity(0.4);
            }
        }
    }

    // ================================================================
    // Construcción vectorial
    // ================================================================

    private Group buildLightVector() {
        Group g = new Group();

        Circle head = new Circle(23, 16, 9);
        head.setFill(Color.rgb(250, 246, 232));

        Rectangle body = new Rectangle(14, 8, 18, 26);
        body.setArcWidth(10);
        body.setArcHeight(10);
        LinearGradient bodyGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 250, 235)),
                new Stop(1, Color.rgb(235, 220, 190)));
        body.setFill(bodyGrad);
        body.setLayoutX(14);
        body.setLayoutY(24);

        // Velo / túnica inferior
        Polygon veil = new Polygon(
                14, 50,
                18 + 4, 62,
                23, 70,
                28 - 4, 62,
                32, 50);
        veil.setFill(Color.rgb(245, 236, 216, 0.95));

        // Corona de luz
        Circle crown = new Circle(23, 7, 3.5);
        crown.setFill(Color.rgb(255, 224, 150));
        DropShadow crownGlow = new DropShadow(8, Color.rgb(255, 230, 170, 0.9));
        crown.setEffect(crownGlow);

        g.getChildren().addAll(body, veil, head, crown);
        g.setLayoutX(-8);
        g.setLayoutY(-8);
        return g;
    }

    private Group buildShadowVector() {
        Group g = new Group();

        // Capucha + cuerpo
        Polygon hood = new Polygon(
                15, 8,
                31, 8,
                33, 30,
                23, 38,
                13, 30);
        hood.setFill(Color.rgb(28, 24, 40));

        Rectangle body = new Rectangle(14, 34, 18, 28);
        body.setArcWidth(10);
        body.setArcHeight(10);
        LinearGradient bodyGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(46, 40, 62)),
                new Stop(1, Color.rgb(16, 13, 24)));
        body.setFill(bodyGrad);
        body.setLayoutX(14);
        body.setLayoutY(34);

        // Ojos violetas
        Ellipse eyeL = new Ellipse(19, 24, 2.6, 3.4);
        Ellipse eyeR = new Ellipse(27, 24, 2.6, 3.4);
        eyeL.setFill(Color.rgb(178, 120, 255));
        eyeR.setFill(Color.rgb(178, 120, 255));

        g.getChildren().addAll(body, hood, eyeL, eyeR);
        g.setLayoutX(-8);
        g.setLayoutY(-8);
        return g;
    }

    private void initSmoke() {
        for (int i = 0; i < 4; i++) {
            Circle c = new Circle(3 + (i % 2) * 1.5);
            c.setFill(Color.rgb(90, 70, 130, 0.5));
            c.setTranslateX(-6 + (i % 3) * 6);
            c.setTranslateY(8 + (i % 3) * 14);
            smokeParticles.add(c);
            root.getChildren().add(c);
        }
    }
}
