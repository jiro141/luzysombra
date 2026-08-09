package com.luzysombra.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.luzysombra.config.ResourcePaths;
import com.luzysombra.entities.Checkpoint;
import com.luzysombra.entities.Collectible;
import com.luzysombra.entities.Door;
import com.luzysombra.entities.Hazard;
import com.luzysombra.entities.Ladder;
import com.luzysombra.entities.MovingPlatform;
import com.luzysombra.entities.Platform;
import com.luzysombra.entities.Switch;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Carga los niveles desde archivos JSON del classpath (/levels/level-N.json).
 * Los niveles pueden editarse sin recompilar la aplicación: solo JSON.
 */
public final class LevelLoader {

    private static final Logger LOG = Logger.getLogger(LevelLoader.class.getName());

    private LevelLoader() {
    }

    public static Level load(int levelNumber) {
        String path = ResourcePaths.levelJson(levelNumber);
        try (InputStream in = LevelLoader.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("No se encontró el nivel: " + path);
            }
            JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();

            Level level = new Level(
                    getString(root, "name", "Nivel " + levelNumber),
                    getDouble(root, "width", 4800),
                    getDouble(root, "height", 1500),
                    getSpawn(root, "spawnLight", "x"),
                    getSpawn(root, "spawnLight", "y"),
                    getSpawn(root, "spawnShadow", "x"),
                    getSpawn(root, "spawnShadow", "y")
            );

            JsonArray objects = root.getAsJsonArray("objects");
            for (var element : objects) {
                JsonObject obj = element.getAsJsonObject();
                parseObject(level, obj);
            }
            return level;
        } catch (Exception ex) {
            LOG.log(java.util.logging.Level.SEVERE, "Error al cargar el nivel " + levelNumber, ex);
            throw new IllegalStateException("No se pudo cargar el nivel " + levelNumber, ex);
        }
    }

    private static void parseObject(Level level, JsonObject obj) {
        String id = getString(obj, "id", "obj");
        String type = getString(obj, "type", "platform");
        String subtype = getString(obj, "subtype", "");
        double x = getDouble(obj, "x", 0);
        double y = getDouble(obj, "y", 0);
        double w = getDouble(obj, "width", 100);
        double h = getDouble(obj, "height", 40);

        switch (type) {
            case "platform" -> level.addPlatform(new Platform(id, x, y, w, h));
            case "moving_platform", "movingPlatform" -> {
                String axis = getString(obj, "axis", "horizontal");
                double range = getDouble(obj, "range", 200);
                double speed = getDouble(obj, "speed", 100);
                MovingPlatform mp = new MovingPlatform(id, x, y, w, h, axis, range, speed);
                String switchId = getString(obj, "switch", "");
                if (!switchId.isBlank()) {
                    mp.setSwitchId(switchId);
                }
                level.addMovingPlatform(mp);
            }
            case "ladder" -> level.addLadder(new Ladder(id, x, y, w, h));
            case "checkpoint", "checkpoint_light" -> level.addCheckpoint(new Checkpoint(id, x, y));
            case "collectible" -> level.addCollectible(new Collectible(id, subtype, x, y));
            case "spikes" -> level.addHazard(new Hazard(id, "spikes", x, y, w, h));
            case "hazard" -> level.addHazard(new Hazard(id, subtype, x, y, w, h));
            case "door" -> level.addDoor(new Door(id, subtype, x, y, w, h));
            case "switch", "pressure_plate" -> {
                String operator = getString(obj, "operator", "any");
                String target = getString(obj, "target", "");
                level.addSwitch(new Switch(id, operator, target, x, y, w, h));
            }
            default -> LOG.warning("Tipo de objeto desconocido en el nivel: " + type + " (id=" + id + ")");
        }
    }

    private static String getString(JsonObject obj, String key, String fallback) {
        return obj.has(key) ? obj.get(key).getAsString() : fallback;
    }

    private static double getDouble(JsonObject obj, String key, double fallback) {
        return obj.has(key) ? obj.get(key).getAsDouble() : fallback;
    }

    private static double getSpawn(JsonObject root, String spawnKey, String coord) {
        if (root.has(spawnKey) && root.getAsJsonObject(spawnKey).has(coord)) {
            return root.getAsJsonObject(spawnKey).get(coord).getAsDouble();
        }
        return 100;
    }
}
