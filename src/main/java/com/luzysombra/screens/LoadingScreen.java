package com.luzysombra.screens;

import com.luzysombra.config.GameConfig;
import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Pantalla de carga inicial: logo, barra de progreso y precarga de recursos.
 * Al terminar, invoca el callback para navegar al menú principal.
 */
public class LoadingScreen extends VBox {

    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label("Cargando recursos…");
    private final Timeline timeline = new Timeline();

    public LoadingScreen(Runnable onComplete) {
        setAlignment(Pos.CENTER);
        setSpacing(28);
        setPrefSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        getStyleClass().add("loading-screen");

        ImageView logo = new ImageView(ResourceManager.getInstance().getImage(ResourcePaths.LOGO));
        logo.setFitWidth(420);
        logo.setPreserveRatio(true);

        progressBar.setPrefWidth(460);
        progressBar.setPrefHeight(14);
        progressBar.getStyleClass().add("loading-bar");
        progressBar.setProgress(0);

        statusLabel.getStyleClass().add("loading-text");

        StackPane barWrap = new StackPane(progressBar);
        barWrap.setPrefWidth(460);
        getChildren().addAll(logo, barWrap, statusLabel);

        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(progressBar.progressProperty(), 0),
                        new KeyValue(statusLabel.textProperty(), "Cargando recursos…")),
                new KeyFrame(Duration.seconds(GameConfig.LOADING_DURATION * 0.55),
                        new KeyValue(progressBar.progressProperty(), 0.72,
                                Interpolator.EASE_OUT),
                        new KeyValue(statusLabel.textProperty(), "Despertando a Luz y Sombra…")),
                new KeyFrame(Duration.seconds(GameConfig.LOADING_DURATION),
                        new KeyValue(progressBar.progressProperty(), 1.0,
                                Interpolator.EASE_OUT),
                        new KeyValue(statusLabel.textProperty(), "Listo"))
        );
        timeline.setOnFinished(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    /** Precarga los recursos y arranca la animación de carga. */
    public void startLoading() {
        // La precarga es rápida; el Timeline da la pausa visual mínima
        ResourceManager.getInstance().preloadAll((fraction, step) -> {
        }, 10);
        timeline.play();
    }
}
