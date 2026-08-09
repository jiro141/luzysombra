package com.luzysombra.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Overlay de victoria: se muestra al completar el nivel con los dos personajes.
 * Resume el tiempo, coleccionables y vidas restantes, y permite avanzar al
 * siguiente nivel, reintentar o volver a la selección.
 */
public class VictoryOverlay extends GameOverlay {

    private final Label statsLabel = title("", "overlay-stats");
    private final GameButton nextButton;

    public VictoryOverlay(Runnable onNext, Runnable onRetry, Runnable onLevelSelect, boolean hasNextLevel) {
        Label title = title("NIVEL COMPLETADO", "overlay-title");
        Label subtitle = title("La luz y la sombra se reencuentran", "overlay-subtitle");

        // Estadísticas
        VBox statsBox = new VBox(6);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getStyleClass().add("overlay-stats-box");
        statsBox.getChildren().add(statsLabel);

        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);

        nextButton = new GameButton("SIGUIENTE NIVEL", GameButton.Variant.PRIMARY, onNext);
        nextButton.setDisable(!hasNextLevel);
        GameButton retry = new GameButton("REINTENTAR", GameButton.Variant.GHOST, onRetry);
        GameButton levels = new GameButton("SELECCIÓN DE NIVELES", GameButton.Variant.DANGER, onLevelSelect);

        buttons.getChildren().addAll(nextButton, retry, levels);
        add(title, subtitle, statsBox, buttons);
    }

    /** Configura las estadísticas mostradas al finalizar. */
    public void setStats(double timeSeconds, int albors, int obsidians, int lightLives, int shadowLives) {
        int totalSeconds = (int) timeSeconds;
        String time = String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
        statsLabel.setText(
                "⏱ Tiempo: " + time
                        + "    ✦ Albores: " + albors + "/" + albors
                        + "    ◆ Obsidianas: " + obsidians + "/" + obsidians
                        + "    ♥ Vidas: " + lightLives + " / " + shadowLives);
    }

    public boolean isNextDisabled() {
        return nextButton.isDisable();
    }
}
