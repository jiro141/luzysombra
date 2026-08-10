package com.luzysombra.tools;

import com.luzysombra.input.InputManager;
import com.luzysombra.navigation.ScreenManager;
import com.luzysombra.persistence.ProgressManager;
import com.luzysombra.screens.GameScreen;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;

/**
 * Herramienta de DIAGNÓSTICO reutilizable: abre el Nivel 1 real, deja correr el
 * game loop ~1 segundo y guarda una captura de la escena en target/diag-level1.png.
 * Imprime el árbol completo del mundo: type real de worldGroup, count de hijos,
 * detalle por hijo (bounds, visible, opacity, clip, translate, imagen), clip y
 * translate del worldGroup, hijos de backgroundLayer y orden del StackPane raíz.
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

        javafx.animation.PauseTransition wait =
                new javafx.animation.PauseTransition(Duration.seconds(1.2));
        wait.setOnFinished(e -> {
            try {
                dumpWorld(game);

                WritableImage snap = scene.snapshot(null);
                File out = new File("target/diag-level1.png");
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

    /** Refleja worldGroup (Pane) y reporta su contenido y los de los contenedores. */
    private void dumpWorld(GameScreen game) {
        try {
            Field f = GameScreen.class.getDeclaredField("worldGroup");
            f.setAccessible(true);
            Object wg = f.get(game);
            System.out.println("=== WORLD-GROUP ===");
            if (wg == null) {
                System.out.println("worldGroup es NULL");
                return;
            }
            Field lf = GameScreen.class.getDeclaredField("level");
            lf.setAccessible(true);
            Object lvl = lf.get(game);
            Field lnf = GameScreen.class.getDeclaredField("levelNumber");
            lnf.setAccessible(true);
            System.out.println("levelNumber=" + lnf.getInt(game)
                    + " level.name=" + (lvl != null
                            ? lvl.getClass().getMethod("getName").invoke(lvl) : "NULL"));
            System.out.println("type=" + wg.getClass().getSimpleName()
                    + " class=" + wg.getClass().getName());
            javafx.scene.layout.Pane world = (javafx.scene.layout.Pane) wg;
            System.out.println("layoutX=" + world.getLayoutX()
                    + " layoutY=" + world.getLayoutY()
                    + " translateX=" + world.getTranslateX()
                    + " translateY=" + world.getTranslateY()
                    + " managed=" + world.isManaged()
                    + " children=" + world.getChildren().size()
                    + " visible=" + world.isVisible()
                    + " opacity=" + world.getOpacity());
            Node clip = world.getClip();
            if (clip != null) {
                System.out.println("clip=" + clip.getClass().getName()
                        + " boundsInLocal=" + clip.getBoundsInLocal()
                        + " layoutPosition=" + clip.getLayoutX() + "," + clip.getLayoutY());
            } else {
                System.out.println("clip=null");
            }
            int i = 0;
            for (Node node : world.getChildren()) {
                var b = node.getBoundsInParent();
                System.out.printf("  [%02d] %-20s %-25s x=%.1f y=%.1f w=%.1f h=%.1f visible=%s opacity=%.2f tx=%.1f ty=%.1f clip=%s%n",
                        i, node.getClass().getSimpleName(), node.getClass().getName(),
                        b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight(),
                        node.isVisible(), node.getOpacity(),
                        node.getTranslateX(), node.getTranslateY(),
                        node.getClip() != null ? node.getClip().getClass().getSimpleName() : "-");
                dumpImageViewIfImage(node, i);
                i++;
            }

            Field bg = GameScreen.class.getDeclaredField("backgroundLayer");
            bg.setAccessible(true);
            System.out.println("=== BACKGROUND-LAYER (StackPane) ===");
            Object bgObj = bg.get(game);
            if (bgObj instanceof StackPane sp) {
                System.out.println("children=" + sp.getChildren().size());
                for (Node n : sp.getChildren()) {
                    System.out.println("  bg-child " + n.getClass().getName()
                            + " bounds=" + n.getBoundsInParent());
                }
            } else if (bgObj == null) {
                System.out.println("backgroundLayer es NULL");
            }

            System.out.println("=== GAME-SCREEN RAIZ (StackPane) orden getChildren ===");
            for (Node n : game.getChildren()) {
                System.out.println("  [" + game.getChildren().indexOf(n) + "] "
                        + n.getClass().getSimpleName() + " " + n.getClass().getName());
            }

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
        } catch (Exception ex) {
            System.out.println("WORLD DUMP FAILED: " + ex);
        }
    }

    /** Recorre recursivamente los hijos buscando ImageView y reporta su estado. */
    private void dumpImageViewIfImage(Node node, int idx) {
        if (node instanceof ImageView iv) {
            reportImageView(iv, "  [" + idx + "] ");
        }
        if (node instanceof javafx.scene.layout.Pane pane) {
            for (Node child : pane.getChildren()) {
                if (child instanceof ImageView iv) {
                    reportImageView(iv, "     descend-");
                } else if (child instanceof javafx.scene.layout.Pane p2) {
                    for (Node gc : p2.getChildren()) {
                        if (gc instanceof ImageView iv2) {
                            reportImageView(iv2, "     descend2-");
                        }
                    }
                }
            }
        } else if (node instanceof javafx.scene.Group group) {
            for (Node child : group.getChildren()) {
                if (child instanceof ImageView iv) {
                    reportImageView(iv, "     descend-");
                }
            }
        }
    }

    private void reportImageView(ImageView iv, String prefix) {
        var img = iv.getImage();
        if (img == null) {
            System.out.println(prefix + "ImageView IMG=NULL width=" + iv.getFitWidth() + " height=" + iv.getFitHeight());
        } else {
            System.out.println(prefix + "ImageView img=" + img.getWidth() + "x" + img.getHeight()
                    + " error=" + img.isError()
                    + " proxPhys=" + img.getProgress()
                    + " fit=" + iv.getFitWidth() + "x" + iv.getFitHeight()
                    + " visible=" + iv.isVisible());
        }
    }

    /** Escribe un WritableImage como PNG sin SwingFXUtils. */
    private void writePng(File out, WritableImage snap) throws Exception {
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