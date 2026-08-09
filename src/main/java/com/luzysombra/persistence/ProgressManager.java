package com.luzysombra.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Guarda el progreso del jugador de forma local en un archivo JSON dentro del
 * directorio personal del usuario (fuera del JAR para que persista entre sesiones).
 * <p>
 * Ruta: {user.home}/.luzysombra/progress.json
 */
public final class ProgressManager {

    private static final Logger LOG = Logger.getLogger(ProgressManager.class.getName());

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "progress.json";

    private Path filePath;
    private ProgressData data;

    public ProgressManager() {
        this.filePath = Path.of(System.getProperty("user.home"), ".luzysombra", FILE_NAME);
        load();
    }

    // ---------------------------------------------------------------
    // Modelo de datos
    // ---------------------------------------------------------------

    private static final class ProgressData {
        int maxUnlockedLevel = 1;
        Map<Integer, Double> bestTimes = new HashMap<>();
        Map<Integer, Integer> bestCollectibles = new HashMap<>();
    }

    // ---------------------------------------------------------------
    // Carga / guardado
    // ---------------------------------------------------------------

    private void load() {
        try {
            if (Files.exists(filePath)) {
                String json = Files.readString(filePath);
                data = GSON.fromJson(json, ProgressData.class);
                if (data == null) {
                    data = new ProgressData();
                }
                if (data.bestTimes == null) {
                    data.bestTimes = new HashMap<>();
                }
                if (data.bestCollectibles == null) {
                    data.bestCollectibles = new HashMap<>();
                }
            } else {
                data = new ProgressData();
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "No se pudo leer el progreso: " + filePath, ex);
            data = new ProgressData();
        }
    }

    public void save() {
        try {
            if (Files.notExists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }
            Files.writeString(filePath, GSON.toJson(data));
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "No se pudo guardar el progreso: " + filePath, ex);
        }
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    public boolean isUnlocked(int levelNumber) {
        return levelNumber <= data.maxUnlockedLevel;
    }

    public int getMaxUnlockedLevel() {
        return data.maxUnlockedLevel;
    }

    public boolean isCompleted(int levelNumber) {
        return data.bestTimes.containsKey(levelNumber);
    }

    /** Mejor tiempo en segundos, o {@code null} si el nivel no fue completado. */
    public Double getBestTime(int levelNumber) {
        return data.bestTimes.get(levelNumber);
    }

    /** Mejor cantidad de coleccionables (0..8), o 0 si nunca se completó. */
    public int getBestCollectibles(int levelNumber) {
        return data.bestCollectibles.getOrDefault(levelNumber, 0);
    }

    // ---------------------------------------------------------------
    // Registro de resultados
    // ---------------------------------------------------------------

    /**
     * Registra la finalización de un nivel. Devuelve true si es la primera vez
     * que se completa (lo que habilita el desbloqueo del siguiente).
     */
    public boolean recordResult(int levelNumber, double timeSeconds, int collectiblesCollected) {
        boolean firstCompletion = !isCompleted(levelNumber);

        Double best = data.bestTimes.get(levelNumber);
        if (best == null || timeSeconds < best) {
            data.bestTimes.put(levelNumber, timeSeconds);
        }
        int bestCollect = data.bestCollectibles.getOrDefault(levelNumber, 0);
        if (collectiblesCollected > bestCollect) {
            data.bestCollectibles.put(levelNumber, collectiblesCollected);
        }

        if (firstCompletion && levelNumber < 999) {
            int next = levelNumber + 1;
            if (next > data.maxUnlockedLevel) {
                data.maxUnlockedLevel = next;
            }
        }
        save();
        return firstCompletion;
    }

    /** Fuerza el desbloqueo de un nivel (uso en desarrollo o debug). */
    public void unlockLevel(int levelNumber) {
        if (levelNumber > data.maxUnlockedLevel) {
            data.maxUnlockedLevel = levelNumber;
            save();
        }
    }

    /** Reinicia todo el progreso. */
    public void resetAll() {
        data = new ProgressData();
        save();
    }

    public Path getFilePath() {
        return filePath;
    }
}
