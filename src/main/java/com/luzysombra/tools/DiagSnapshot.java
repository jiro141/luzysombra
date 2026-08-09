package com.luzysombra.tools;

import com.luzysombra.input.InputManager;
import com.luzysombra.navigation.ScreenManager;
import com.luzysombra.persistence.ProgressManager;
import com.luzysombra.screens.GameScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.Field;

/**
 * Herramienta de DIAGNÓSTICO (temporal): abre el Nivel 1 real, deja correr el
 * game loop ~1 segundo y guarda una captura de la escena en target/diag-level1.png.
 * También imprime el árbol del worldGroup por reflexión para confirmar que las
 * entidades existen y tienen dimensión.
 *
 * Ejecutar: mvn -q compile exec:java -Dexec.mainClass=com.luzysombra.tools.DiagSnapshot
 * (JavaFX: usar javafx:run con mainClass override)
 */
public final class DiagSnapshot extends Application {

    @Override
    public void start(Stage stage) {
        ScreenManager screens = new ScreenManager();
        InputManager input = new InputManager();
        ProgressManager progress = new ProgressManager();

        Runnable noop = () -> { };
        GameScreen game = new GameScreen(1, screens, input, progress, noop, noop, noop);

        StackPane host = new StackPane(game);
        host.setPrefSize(1600, 900);
        Scene scene = new Scene(host, 1600, 900);

        input.attach(scene);
        stage.setScene(scene);

        // Deja correr el game loop y el layout un segundo antes de capturar
        javafx.animation.PauseTransition wait =
                new javafx.animation.PauseTransition(Duration.seconds(1.2));
        wait.setOnFinished(e -> {
            try {
                dumpWorld(game);
                WritableImage snap = scene.snapshot(null);
                File out = new File("target/diag-level1.png");
                out.getParentFile().mkdirs();
                writePng(out, snap);
                System.out.println("SNAPSHOT: " + out.getAbsolutePath()
                        + " (" + snap.getWidth() + "x" + snap.getHeight() + ")");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.flush();
            System.exit(0);
        });
        stage.show();
        wait.play();

        // Red de seguridad: fuerza salida pase lo que pase
        new Thread(() -> {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ignored) {
            }
            System.out.println("TIMEOUT-FORCE-EXIT");
            System.out.flush();
            System.exit(2);
        }).start();
    }

    /** Refleja el worldGroup privado del GameScreen y reporta su contenido. */
    private void dumpWorld(GameScreen game) {
        try {
            Field f = GameScreen.class.getDeclaredField("worldGroup");
            f.setAccessible(true);
            javafx.scene.Group world = (javafx.scene.Group) f.get(game);
            System.out.println("WORLD: layoutX=" + world.getLayoutX()
                    + " layoutY=" + world.getLayoutY()
                    + " managed=" + world.isManaged()
                    + " children=" + world.getChildren().size()
                    + " visible=" + world.isVisible()
                    + " opacity=" + world.getOpacity());
            for (var node : world.getChildren()) {
                var b = node.getBoundsInParent();
                System.out.printf("  node %-45s x=%.0f y=%.0f w=%.0f h=%.0f%n",
                        node.getClass().getSimpleName(), b.getMinX(), b.getMinY(),
                        b.getWidth(), b.getHeight());
            }
            // Cámara real (para saber dónde está el viewport)
            Field cam = GameScreen.class.getDeclaredField("camera");
            cam.setAccessible(true);
            Object c = cam.get(game);
            if (c != null) {
                var m = c.getClass().getDeclaredMethod("getX");
                var m2 = c.getClass().getDeclaredMethod("getY");
                System.out.println("CAMERA: x=" + m.invoke(c) + " y=" + m2.invoke(c));
            } else {
                System.out.println("CAMERA: null");
            }
            // Snapshot del MUNDO SOLO (nodo directo, sin fondo ni HUD)
            javafx.scene.image.WritableImage wimg = world.snapshot(null, null);
            File f2 = new File("target/diag-world-only.png");
            writePng(f2, wimg);
        } catch (Exception ex) {
            System.out.println("WORLD DUMP FAILED: " + ex);
        }
    }

    /** Escribe un WritableImage como PNG sin SwingFXUtils. */
    private void writePng(File out, javafx.scene.image.WritableImage snap) throws Exception {
        out.getParentFile().mkdirs();
        javafx.scene.image.PixelReader pr = snap.getPixelReader();
        java.awt.image.BufferedImage bi =
                new java.awt.image.BufferedImage((int) snap.getWidth(),
                        (int) snap.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < snap.getHeight(); y++) {
            for (int x = 0; x < snap.getWidth(); x++) {
                bi.setRGB(x, y, pr.getArgb(x, y));
            }
        }
        ImageIO.write(bi, "png", out);
        System.out.println("PNG: " + out.getAbsolutePath()
                + " (" + snap.getWidth() + "x" + snap.getHeight() + ")");
    }

    public static void main(String[] args) {
        Application.launch(DiagSnapshot.class, args);
    }
}
