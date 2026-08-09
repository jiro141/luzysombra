package com.luzysombra.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Random;

/**
 * Campo de partículas decorativas (polvo de luz y ceniza de sombra) que flota
 * suavemente. Se usa en el menú principal y la selección de niveles para dar
 * vida a la estética mística sin afectar la jugabilidad.
 */
public class ParticleField extends Pane {

    private static final int COUNT = 26;

    private final Random random = new Random();
    private final Circle[] particles = new Circle[COUNT];
    private final double[] speeds = new double[COUNT];
    private final double[] phases = new double[COUNT];
    private final AnimationTimer timer;

    public ParticleField() {
        setMouseTransparent(true);
        setPickOnBounds(false);

        for (int i = 0; i < COUNT; i++) {
            Circle c = new Circle(1.5 + random.nextDouble() * 3.0);
            boolean light = random.nextBoolean();
            c.setFill(light
                    ? Color.rgb(240, 222, 180, 0.35 + random.nextDouble() * 0.4)
                    : Color.rgb(140, 110, 190, 0.25 + random.nextDouble() * 0.35));
            c.setOpacity(0.3 + random.nextDouble() * 0.6);
            c.setLayoutX(random.nextDouble() * 1600);
            c.setLayoutY(random.nextDouble() * 900);
            phases[i] = random.nextDouble() * Math.PI * 2;
            speeds[i] = 6 + random.nextDouble() * 16;
            particles[i] = c;
            getChildren().add(c);
        }

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double t = now / 1_000_000_000.0;
                for (int i = 0; i < COUNT; i++) {
                    Circle c = particles[i];
                    double y = c.getLayoutY() - speeds[i] * 0.016;
                    if (y < -10) {
                        y = 910;
                    }
                    c.setLayoutY(y);
                    c.setLayoutX(c.getLayoutX() + Math.sin(t * 0.8 + phases[i]) * 0.25);
                    c.setOpacity(0.3 + 0.4 * (0.5 + 0.5 * Math.sin(t * 1.2 + phases[i])));
                }
            }
        };
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}
