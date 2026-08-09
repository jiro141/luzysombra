package com.luzysombra.screens;

import com.luzysombra.config.GameConfig;
import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import com.luzysombra.ui.GameButton;
import com.luzysombra.ui.ParticleField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Menú principal: fondo con el título y los personajes, botones (JUGAR,
 * TUTORIAL, SALIR), partículas decorativas y control de volumen de la música.
 * <p>
 * El fondo (imagen + viñeta) y las partículas son mouse-transparent: nunca
 * interceptan los clics sobre los botones. SALIR abre una confirmación interna
 * (vía overlay) en lugar de salir directamente.
 */
public class MainMenuScreen extends StackPane {

    private static final double BUTTON_WIDTH = 300;
    private static final double BUTTON_HEIGHT = 60;

    private final ParticleField particles = new ParticleField();
    private final Slider musicSlider = new Slider(0, 1, 0.55);
    private GameButton muteButton;
    private double previousVolume = 0.55;
    private boolean muted = false;

    public MainMenuScreen(Runnable onPlay, Runnable onTutorial, Runnable onExit) {
        setPrefSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        getStyleClass().add("menu-screen");

        // Fondo decorativo (no intercepta clics)
        ImageView background = new ImageView(ResourceManager.getInstance().getImage(ResourcePaths.BACKGROUND_MENU));
        background.setFitWidth(GameConfig.LOGICAL_WIDTH);
        background.setFitHeight(GameConfig.LOGICAL_HEIGHT);
        background.setPreserveRatio(false);
        background.setMouseTransparent(true);

        Rectangle vignette = new Rectangle(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        vignette.setFill(Color.rgb(18, 12, 8, 0.35));
        vignette.setMouseTransparent(true);

        // Contenido inferior: los botones van abajo porque el fondo ya incluye el título
        VBox content = new VBox(22);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(0, 40, 120, 40));
        // Clave: limitar el alto al tamaño preferido para que StackPane pueda anclarlo abajo
        content.setMaxHeight(Region.USE_PREF_SIZE);
        content.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(content, Pos.BOTTOM_CENTER);

        // Botones alineados: mismo ancho y mismo alto
        VBox buttons = new VBox(18);
        buttons.setAlignment(Pos.CENTER);

        GameButton play = new GameButton("JUGAR", onPlay);
        play.getStyleClass().add("game-button-menu-play");
        GameButton tutorial = new GameButton("TUTORIAL", GameButton.Variant.GHOST, onTutorial);
        GameButton exit = new GameButton("SALIR", GameButton.Variant.DANGER, onExit);

        for (GameButton b : new GameButton[]{play, tutorial, exit}) {
            b.setMinWidth(BUTTON_WIDTH);
            b.setMaxWidth(BUTTON_WIDTH);
            b.setMinHeight(BUTTON_HEIGHT);
            b.setPrefHeight(BUTTON_HEIGHT);
        }
        buttons.getChildren().addAll(play, tutorial, exit);

        // Separador: separa claramente el control de música del botón SALIR
        Rectangle divider = new Rectangle(BUTTON_WIDTH, 1.5);
        divider.setFill(Color.rgb(184, 155, 94, 0.4));

        // Control de volumen + mute
        Label volumeLabel = new Label("Música");
        volumeLabel.getStyleClass().add("volume-label");
        musicSlider.getStyleClass().add("volume-slider");
        musicSlider.setPrefWidth(180);
        musicSlider.setFocusTraversable(false);
        musicSlider.valueProperty().addListener((obs, o, n) -> {
            if (muted && n.doubleValue() > 0) {
                setMuted(false);
            }
            ResourceManager.getInstance().setMusicVolume(n.doubleValue());
        });

        muteButton = new GameButton("🔊", GameButton.Variant.GHOST, this::toggleMute);
        muteButton.getStyleClass().add("volume-mute-button");
        muteButton.setTooltip(new Tooltip("Silenciar / restaurar música"));

        HBox volumeBox = new HBox(12, volumeLabel, musicSlider, muteButton);
        volumeBox.setAlignment(Pos.CENTER);

        content.getChildren().addAll(buttons, divider, volumeBox);

        getChildren().addAll(background, vignette, particles, content);

        // Arranca la música ambiental (solo la primera vez)
        if (!musicStarted) {
            musicStarted = true;
            ResourceManager.getInstance().playMusicLoop();
        }
    }

    private static boolean musicStarted = false;

    private void toggleMute() {
        if (muted) {
            setMuted(false);
            musicSlider.setValue(previousVolume);
        } else {
            previousVolume = musicSlider.getValue();
            setMuted(true);
            musicSlider.setValue(0);
        }
    }

    private void setMuted(boolean m) {
        muted = m;
        muteButton.setText(m ? "🔇" : "🔊");
        muteButton.getStyleClass().remove("muted");
        if (m) {
            muteButton.getStyleClass().add("muted");
        }
    }

    public void startParticles() {
        particles.start();
    }

    public void stopParticles() {
        particles.stop();
    }

    public void syncVolume() {
        musicSlider.setValue(ResourceManager.getInstance().getMusicVolume());
    }
}
