package com.luzysombra.screens;

import com.luzysombra.config.GameConfig;
import com.luzysombra.config.ResourcePaths;
import com.luzysombra.persistence.ProgressManager;
import com.luzysombra.resources.ResourceManager;
import com.luzysombra.ui.GameButton;
import com.luzysombra.ui.ParticleField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.function.IntConsumer;

/**
 * Selección de niveles: cinco tarjetas con miniatura, estado, candado,
 * mejor tiempo y coleccionables. Los niveles se desbloquean al completar
 * el anterior (el progreso se guarda en ProgressManager).
 * <p>
 * La capa de fondo (imagen + atenuación + partículas) va DETRÁS del contenido
 * y es mouse-transparent: toda el área de cada tarjeta es el botón. Las tarjetas
 * bloqueadas no responden al mouse y muestran cursor por defecto.
 */
public class LevelSelectionScreen extends StackPane {

    private final ProgressManager progress;
    private final IntConsumer onPlayLevel;
    private final ParticleField particles = new ParticleField();
    private final StackPane bgLayer = new StackPane();

    public LevelSelectionScreen(ProgressManager progress, IntConsumer onPlayLevel, Runnable onBackToMenu) {
        this.progress = progress;
        this.onPlayLevel = onPlayLevel;

        setPrefSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        getStyleClass().add("level-select-screen");

        addBackground();

        Label title = new Label("SELECCIÓN DE NIVELES");
        title.getStyleClass().add("screen-title");

        HBox cards = new HBox(26);
        cards.setAlignment(Pos.CENTER);
        for (int i = 1; i <= GameConfig.TOTAL_LEVELS; i++) {
            cards.getChildren().add(createCard(i));
        }

        GameButton back = new GameButton("VOLVER AL MENÚ", GameButton.Variant.GHOST, onBackToMenu);
        back.setMinWidth(260);
        back.setMaxWidth(260);

        VBox content = new VBox(28);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(36));
        content.getChildren().addAll(title, cards, back);

        getChildren().addAll(bgLayer, content);
    }

    private void addBackground() {
        bgLayer.setPrefSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        bgLayer.setMouseTransparent(true);
        ImageView bg = new ImageView(ResourceManager.getInstance().getImage(ResourcePaths.BACKGROUND_MENU));
        bg.setFitWidth(GameConfig.LOGICAL_WIDTH);
        bg.setFitHeight(GameConfig.LOGICAL_HEIGHT);
        bg.setPreserveRatio(false);
        bg.setMouseTransparent(true);
        Rectangle dim = new Rectangle(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        dim.setFill(Color.rgb(14, 10, 8, 0.78));
        dim.setMouseTransparent(true);
        bgLayer.getChildren().addAll(bg, dim, particles);
    }

    public void startParticles() {
        particles.start();
    }

    public void stopParticles() {
        particles.stop();
    }

    private VBox createCard(int levelNumber) {
        boolean unlocked = progress.isUnlocked(levelNumber);
        boolean completed = progress.isCompleted(levelNumber);

        VBox card = new VBox(10);
        card.setPrefWidth(252);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("level-card");
        card.getStyleClass().add(unlocked ? "level-card-unlocked" : "level-card-locked");
        card.setCursor(unlocked ? Cursor.HAND : Cursor.DEFAULT);

        StackPane thumbWrap = new StackPane();
        thumbWrap.setPrefSize(232, 130);
        ImageView thumb = new ImageView(ResourceManager.getInstance().getImage(ResourcePaths.levelThumbnail(levelNumber)));
        thumb.setFitWidth(232);
        thumb.setFitHeight(130);
        thumb.setPreserveRatio(false);
        thumbWrap.getChildren().add(thumb);

        if (!unlocked) {
            Rectangle shade = new Rectangle(232, 130);
            shade.setFill(Color.rgb(10, 8, 14, 0.72));
            thumbWrap.getChildren().add(shade);
            ImageView lock = new ImageView(ResourceManager.getInstance().getImage(ResourcePaths.LOCK));
            lock.setFitWidth(42);
            lock.setFitHeight(42);
            lock.setPreserveRatio(true);
            thumbWrap.getChildren().add(lock);
        } else if (!completed) {
            Label newTag = new Label("NUEVO");
            newTag.getStyleClass().add("new-tag");
            StackPane.setAlignment(newTag, Pos.TOP_RIGHT);
            newTag.setTranslateX(-8);
            newTag.setTranslateY(8);
            thumbWrap.getChildren().add(newTag);
        }

        Label name = new Label("Nivel " + levelNumber);
        name.getStyleClass().add("level-card-title");

        Label status = new Label(completed ? "COMPLETADO" : (unlocked ? "DISPONIBLE" : "BLOQUEADO"));
        status.getStyleClass().add(completed ? "status-completed" : (unlocked ? "status-available" : "status-locked"));

        VBox details = new VBox(4);
        details.setAlignment(Pos.CENTER);
        if (completed) {
            Label time = new Label("Mejor tiempo: " + formatTime(progress.getBestTime(levelNumber)));
            time.getStyleClass().add("level-card-detail");
            Label collect = new Label("Coleccionables: " + progress.getBestCollectibles(levelNumber) + "/8");
            collect.getStyleClass().add("level-card-detail");
            details.getChildren().addAll(time, collect);
        } else if (!unlocked) {
            Label hint = new Label("Completa el nivel anterior");
            hint.getStyleClass().add("level-card-detail");
            details.getChildren().add(hint);
        }

        card.getChildren().addAll(thumbWrap, name, status, details);

        // Solo las tarjetas desbloqueadas reaccionan al mouse: toda la tarjeta
        // es el botón, con efecto de pulsación y disparo único al soltar.
        if (unlocked) {
            card.setOnMousePressed(e -> {
                card.setScaleX(0.97);
                card.setScaleY(0.97);
            });
            card.setOnMouseReleased(e -> {
                card.setScaleX(1.0);
                card.setScaleY(1.0);
                if (card.isHover()) {
                    onPlayLevel.accept(levelNumber);
                }
            });
            card.setOnMouseExited(e -> {
                card.setScaleX(1.0);
                card.setScaleY(1.0);
            });
        }
        return card;
    }

    private static String formatTime(double seconds) {
        int total = (int) seconds;
        return String.format("%d:%02d", total / 60, total % 60);
    }
}
