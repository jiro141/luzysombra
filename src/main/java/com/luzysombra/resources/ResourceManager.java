package com.luzysombra.resources;

import com.luzysombra.config.ResourcePaths;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carga, cachea y gestiona todos los recursos del juego (imágenes y audio).
 * Las imágenes se cargan siempre desde el classpath con rutas que comienzan con "/".
 * Si un recurso falta, se genera un placeholder profesional y se registra el error:
 * el juego NUNCA se cae por un recurso ausente.
 */
public final class ResourceManager {

    private static final Logger LOG = Logger.getLogger(ResourceManager.class.getName());

    private static final String[] REQUIRED_IMAGES = {
            ResourcePaths.BACKGROUND_MENU,
            ResourcePaths.backgroundLevel(1),
            ResourcePaths.backgroundLevel(2),
            ResourcePaths.backgroundLevel(3),
            ResourcePaths.backgroundLevel(4),
            ResourcePaths.backgroundLevel(5),
            ResourcePaths.LOGO,
            ResourcePaths.PAUSE_ICON,
            ResourcePaths.ARROW_LEFT,
            ResourcePaths.ARROW_RIGHT,
            ResourcePaths.LOCK,
            ResourcePaths.LIGHT_IDLE,
            ResourcePaths.LIGHT_WALK,
            ResourcePaths.LIGHT_RUN,
            ResourcePaths.LIGHT_JUMP,
            ResourcePaths.LIGHT_FALL,
            ResourcePaths.SHADOW_IDLE,
            ResourcePaths.SHADOW_WALK,
            ResourcePaths.SHADOW_RUN,
            ResourcePaths.SHADOW_JUMP,
            ResourcePaths.SHADOW_FALL,
            ResourcePaths.ALBOR,
            ResourcePaths.OBSIDIAN,
            ResourcePaths.SPIKES,
            ResourcePaths.HAZARD_LIGHT,
            ResourcePaths.HAZARD_SHADOW,
            ResourcePaths.PLATFORM,
            ResourcePaths.MOVING_PLATFORM,
            ResourcePaths.LADDER,
            ResourcePaths.DOOR_LIGHT_CLOSED,
            ResourcePaths.DOOR_LIGHT_OPEN,
            ResourcePaths.DOOR_SHADOW_CLOSED,
            ResourcePaths.DOOR_SHADOW_OPEN,
            ResourcePaths.levelThumbnail(1),
            ResourcePaths.levelThumbnail(2),
            ResourcePaths.levelThumbnail(3),
            ResourcePaths.levelThumbnail(4),
            ResourcePaths.levelThumbnail(5)
    };

    private static final String[] REQUIRED_AUDIO = {
            ResourcePaths.SOUND_CLICK,
            ResourcePaths.SOUND_JUMP,
            ResourcePaths.SOUND_COLLECT_LIGHT,
            ResourcePaths.SOUND_COLLECT_SHADOW,
            ResourcePaths.SOUND_HURT,
            ResourcePaths.SOUND_DOOR,
            ResourcePaths.SOUND_VICTORY,
            ResourcePaths.SOUND_GAME_OVER,
            ResourcePaths.SOUND_CHECKPOINT,
            ResourcePaths.MUSIC_AMBIENT
    };

    private static ResourceManager instance;

    private final Map<String, Image> imageCache = new HashMap<>();
    private final Map<String, Image> scaledImageCache = new HashMap<>();
    private final Map<String, MediaPlayer> audioCache = new HashMap<>();
    private final Set<String> missingImages = new HashSet<>();
    private double musicVolume = 0.55;
    private double sfxVolume = 0.75;

    private ResourceManager() {
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    // ================================================================
    // Imágenes
    // ================================================================

    public Image getImage(String path) {
        return imageCache.computeIfAbsent(path, this::loadImage);
    }

    /** Indica si una imagen no existe (se usó placeholder). Útil para elegir renderizado alternativo. */
    public boolean isMissing(String path) {
        getImage(path); // asegura el intento de carga
        return missingImages.contains(path);
    }

    /** Igual que {@link #isMissing(String)} pero sin cargar la imagen a resolución completa. */
    public boolean isMissingScaled(String path, double width, double height) {
        getImage(path, width, height);
        return missingImages.contains(path);
    }

    private Image loadImage(String path) {
        try (InputStream stream = ResourceManager.class.getResourceAsStream(path)) {
            if (stream == null) {
                missingImages.add(path);
                LOG.warning("No se encontró la imagen: " + path + " — se usará un placeholder.");
                return createPlaceholderImage(path);
            }
            Image img = new Image(stream);
            if (img.isError()) {
                missingImages.add(path);
                LOG.warning("Imagen con errores: " + path + " — se usará un placeholder.");
                return createPlaceholderImage(path);
            }
            return img;
        } catch (Exception ex) {
            missingImages.add(path);
            LOG.log(Level.WARNING, "Error cargando la imagen: " + path, ex);
            return createPlaceholderImage(path);
        }
    }

    /**
     * Carga (y cachea) una imagen escalada a la resolución indicada, preservando
     * la relación de aspecto. Permite usar sprites de alta resolución (p. ej.
     * 1414x2000) sin inflar el heap: solo se mantiene en memoria la versión
     * reducida realmente mostrada.
     */
    public Image getImage(String path, double width, double height) {
        String key = path + "|" + (int) width + "x" + (int) height;
        return scaledImageCache.computeIfAbsent(key, k -> loadScaledImage(path, width, height));
    }

    private Image loadScaledImage(String path, double width, double height) {
        try (InputStream stream = ResourceManager.class.getResourceAsStream(path)) {
            if (stream == null) {
                missingImages.add(path);
                LOG.warning("No se encontró la imagen (escalada): " + path + " — se usará un placeholder.");
                return createPlaceholderImage(path);
            }
            // preserveRatio=true, smooth=true para ajustar dentro de la caja pedida.
            Image img = new Image(stream, width, height, true, true);
            if (img.isError()) {
                missingImages.add(path);
                LOG.warning("Imagen con errores (escalada): " + path + " — se usará un placeholder.");
                return createPlaceholderImage(path);
            }
            return img;
        } catch (Exception ex) {
            missingImages.add(path);
            LOG.log(Level.WARNING, "Error cargando la imagen (escalada): " + path, ex);
            return createPlaceholderImage(path);
        }
    }

    /** Placeholder profesional: panel oscuro con filigrana de luz y sombra. */
    private Image createPlaceholderImage(String path) {
        int w = 128;
        int h = 128;
        Canvas canvas = new Canvas(w, h);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.rgb(58, 43, 31, 0.9));
        g.fillRoundRect(2, 2, w - 4, h - 4, 18, 18);
        g.setStroke(Color.rgb(184, 155, 94, 0.86));
        g.setLineWidth(3);
        g.strokeRoundRect(2, 2, w - 4, h - 4, 18, 18);
        // Símbolo: media luna blanca + media luna violeta
        g.setFill(Color.rgb(245, 236, 216, 0.78));
        g.fillArc(w / 2.0 - 26, h / 2.0 - 26, 52, 52, 180, 180, ArcType.ROUND);
        g.setFill(Color.rgb(122, 92, 160, 0.86));
        g.fillArc(w / 2.0 - 26, h / 2.0 - 26, 52, 52, 0, 180, ArcType.ROUND);
        g.setStroke(Color.rgb(40, 30, 22));
        g.setLineWidth(2);
        g.strokeOval(w / 2.0 - 26, h / 2.0 - 26, 52, 52);
        g.setFill(Color.rgb(245, 236, 216, 0.66));
        g.setFont(Font.font("Serif", FontWeight.BOLD, 11));
        g.fillText("Luz y Sombra", 22, h - 14);
        return canvas.snapshot(null, null);
    }

    // ================================================================
    // Audio
    // ================================================================

    private MediaPlayer getMediaPlayer(String path) {
        return audioCache.computeIfAbsent(path, this::loadMediaPlayer);
    }

    private MediaPlayer loadMediaPlayer(String path) {
        try {
            var url = Objects.requireNonNull(ResourceManager.class.getResource(path),
                    "Audio no encontrado: " + path).toExternalForm();
            MediaPlayer player = new MediaPlayer(new Media(url));
            player.setVolume(sfxVolume);
            return player;
        } catch (Exception ex) {
            LOG.warning("No se pudo cargar el audio: " + path + " — el juego continuará en silencio.");
            return null;
        }
    }

    /** Reproduce un efecto de sonido corto (se reinicia si ya estaba sonando). */
    public void playSound(String path) {
        MediaPlayer player = getMediaPlayer(path);
        if (player != null) {
            player.stop();
            player.seek(javafx.util.Duration.ZERO);
            player.setVolume(sfxVolume);
            player.play();
        }
    }

    /** Inicia la música ambiental en bucle (idempotente). */
    public void playMusicLoop() {
        MediaPlayer player = getMediaPlayer(ResourcePaths.MUSIC_AMBIENT);
        if (player != null) {
            player.stop();
            player.setVolume(musicVolume);
            player.setCycleCount(MediaPlayer.INDEFINITE);
            player.play();
        }
    }

    public void stopMusic() {
        MediaPlayer player = audioCache.get(ResourcePaths.MUSIC_AMBIENT);
        if (player != null) {
            player.stop();
        }
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(double musicVolume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, musicVolume));
        MediaPlayer player = audioCache.get(ResourcePaths.MUSIC_AMBIENT);
        if (player != null) {
            player.setVolume(this.musicVolume);
        }
    }

    public double getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(double sfxVolume) {
        this.sfxVolume = Math.max(0.0, Math.min(1.0, sfxVolume));
    }

    // ================================================================
    // Precarga
    // ================================================================

    /**
     * Precarga todos los recursos con reporte de progreso.
     * totalSteps cuenta imágenes + audios; el progreso avanza en cada recurso cargado.
     */
    public void preloadAll(ProgressCallback progress, int totalSteps) {
        int done = 0;
        int total = REQUIRED_IMAGES.length + REQUIRED_AUDIO.length;
        for (String imagePath : REQUIRED_IMAGES) {
            preloadImage(imagePath);
            done++;
            progress.onProgress((double) done / total, totalSteps);
        }
        for (String audioPath : REQUIRED_AUDIO) {
            getMediaPlayer(audioPath);
            done++;
            progress.onProgress((double) done / total, totalSteps);
        }
    }

    /**
     * Precarga una imagen; los sprites de personajes (fuente 1414x2000) se cargan
     * escalados para no inflar el heap, el resto a resolución completa.
     */
    private void preloadImage(String imagePath) {
        if (imagePath.contains("/characters/")) {
            getImage(imagePath, 96, 128);
        } else {
            getImage(imagePath);
        }
    }

    /** Libera todos los recursos de audio al cerrar la aplicación. */
    public void disposeAll() {
        for (MediaPlayer player : audioCache.values()) {
            if (player != null) {
                player.stop();
                player.dispose();
            }
        }
        audioCache.clear();
    }

    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(double fraction, int step);
    }
}
