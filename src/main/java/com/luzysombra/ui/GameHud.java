package com.luzysombra.ui;

import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * HUD (Heads-Up Display) del nivel: vidas de Luz y Sombra, contadores de
 * coleccionables, nombre del nivel, tiempo y botón de pausa.
 * <p>
 * Se muestra como una barra superior dentro de la pantalla de juego
 * (sin ventanas ni diálogos).
 */
public class GameHud extends HBox {

    private final Label lightHearts = label("♥♥♥", "hud-light");
    private final Label alborCounter = label("Albores: 0/0", "hud-albor");
    private final Label levelName = label("Nivel 1", "hud-level");
    private final Label obsidianCounter = label("Obsidianas: 0/0", "hud-obsidian");
    private final Label shadowHearts = label("♥♥♥", "hud-shadow");
    private final Label timerLabel = label("0:00", "hud-timer");

    public GameHud(Runnable onPause) {
        setAlignment(Pos.CENTER);
        setSpacing(22);
        setPadding(new Insets(12, 20, 12, 20));
        getStyleClass().add("hud");

        // Columna Luz
        VBox lightBox = new VBox(2, label("LUZ", "hud-caption"), lightHearts, alborCounter);
        lightBox.setAlignment(Pos.CENTER);
        lightBox.setStyle("-fx-padding: 0 18 0 0;");

        // Columna Sombra
        VBox shadowBox = new VBox(2, label("SOMBRA", "hud-caption"), shadowHearts, obsidianCounter);
        shadowBox.setAlignment(Pos.CENTER);
        shadowBox.setStyle("-fx-padding: 0 0 0 18;");

        // Centro: nivel y tiempo
        VBox centerBox = new VBox(2, levelName, timerLabel);
        centerBox.setAlignment(Pos.CENTER);

        // Espaciador flexible y botón de pausa (GameButton: disparo único con mouse)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        GameButton pauseButton;
        if (!ResourceManager.getInstance().isMissing(ResourcePaths.PAUSE_ICON)) {
            ImageView icon = new ImageView(ResourceManager.getInstance().getImage(ResourcePaths.PAUSE_ICON));
            icon.setFitWidth(22);
            icon.setFitHeight(22);
            pauseButton = new GameButton("", GameButton.Variant.GHOST, () -> {
                if (onPause != null) {
                    onPause.run();
                }
            });
            pauseButton.setGraphic(icon);
        } else {
            pauseButton = new GameButton("II", GameButton.Variant.GHOST, () -> {
                if (onPause != null) {
                    onPause.run();
                }
            });
        }
        pauseButton.getStyleClass().add("hud-pause-button");
        pauseButton.setTooltip(new javafx.scene.control.Tooltip("Pausar (ESC)"));

        getChildren().addAll(lightBox, centerBox, spacer, shadowBox, pauseButton);
    }

    public void setLives(int lightLives, int shadowLives) {
        lightHearts.setText(hearts(lightLives));
        shadowHearts.setText(hearts(shadowLives));
    }

    public void setCollectibles(int albors, int totalAlbors, int obsidians, int totalObsidians) {
        alborCounter.setText("Albores: " + albors + "/" + totalAlbors);
        obsidianCounter.setText("Obsidianas: " + obsidians + "/" + totalObsidians);
    }

    public void setLevelName(String name) {
        levelName.setText(name);
    }

    public void setTime(double seconds) {
        int totalSeconds = (int) seconds;
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        timerLabel.setText(String.format("%d:%02d", minutes, secs));
    }

    private static String hearts(int lives) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lives; i++) {
            sb.append('♥');
        }
        for (int i = lives; i < 3; i++) {
            sb.append('♡');
        }
        return sb.toString();
    }

    private static Label label(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }
}
