package com.luzysombra.game;

import com.luzysombra.entities.Checkpoint;
import com.luzysombra.entities.Collectible;
import com.luzysombra.entities.Door;
import com.luzysombra.entities.Hazard;
import com.luzysombra.entities.Ladder;
import com.luzysombra.entities.MovingPlatform;
import com.luzysombra.entities.Platform;
import com.luzysombra.entities.Switch;
import javafx.scene.Group;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nivel cargado: sus dimensiones, puntos de aparición y todas
 * sus entidades. El {@link Group} del mundo agrupa las vistas de todas las
 * entidades y se desplaza con la cámara.
 */
public class Level {

    private final String name;
    private final double width;
    private final double height;
    private final double spawnLightX;
    private final double spawnLightY;
    private final double spawnShadowX;
    private final double spawnShadowY;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<MovingPlatform> movingPlatforms = new ArrayList<>();
    private final List<Ladder> ladders = new ArrayList<>();
    private final List<Hazard> hazards = new ArrayList<>();
    private final List<Collectible> collectibles = new ArrayList<>();
    private final List<Door> doors = new ArrayList<>();
    private final List<Checkpoint> checkpoints = new ArrayList<>();
    private final List<Switch> switches = new ArrayList<>();

    private final Group worldGroup = new Group();
    private boolean worldBuilt;

    public Level(String name, double width, double height,
                 double spawnLightX, double spawnLightY,
                 double spawnShadowX, double spawnShadowY) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.spawnLightX = spawnLightX;
        this.spawnLightY = spawnLightY;
        this.spawnShadowX = spawnShadowX;
        this.spawnShadowY = spawnShadowY;
    }

    // ---------------------------------------------------------------
    // Registro de entidades
    // ---------------------------------------------------------------

    public void addPlatform(Platform p) {
        platforms.add(p);
    }

    public void addMovingPlatform(MovingPlatform p) {
        movingPlatforms.add(p);
    }

    public void addLadder(Ladder l) {
        ladders.add(l);
    }

    public void addHazard(Hazard h) {
        hazards.add(h);
    }

    public void addCollectible(Collectible c) {
        collectibles.add(c);
    }

    public void addDoor(Door d) {
        doors.add(d);
    }

    public void addCheckpoint(Checkpoint c) {
        checkpoints.add(c);
    }

    public void addSwitch(Switch s) {
        switches.add(s);
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    /** Todas las superficies sólidas (plataformas fijas y móviles). */
    public List<com.luzysombra.entities.GameObject> getSolids() {
        List<com.luzysombra.entities.GameObject> solids =
                new ArrayList<>(platforms.size() + movingPlatforms.size());
        solids.addAll(platforms);
        solids.addAll(movingPlatforms);
        return solids;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public List<MovingPlatform> getMovingPlatforms() {
        return movingPlatforms;
    }

    public List<Ladder> getLadders() {
        return ladders;
    }

    public List<Hazard> getHazards() {
        return hazards;
    }

    public List<Collectible> getCollectibles() {
        return collectibles;
    }

    public List<Door> getDoors() {
        return doors;
    }

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public List<Switch> getSwitches() {
        return switches;
    }

    /** Busca una plataforma móvil por su id (para asociarla a un interruptor). */
    public MovingPlatform getMovingPlatformById(String id) {
        for (MovingPlatform mp : movingPlatforms) {
            if (mp.getId().equals(id)) {
                return mp;
            }
        }
        return null;
    }

    public Door getLightDoor() {
        for (Door d : doors) {
            if ("light".equals(d.getSubtype())) {
                return d;
            }
        }
        return doors.isEmpty() ? null : doors.get(0);
    }

    public Door getShadowDoor() {
        for (Door d : doors) {
            if ("shadow".equals(d.getSubtype())) {
                return d;
            }
        }
        return doors.isEmpty() ? null : doors.get(doors.size() - 1);
    }

    public int countAlbor() {
        int n = 0;
        for (Collectible c : collectibles) {
            if (c.getCollectibleType() == Collectible.CollectibleType.ALBOR) {
                n++;
            }
        }
        return n;
    }

    public int countObsidian() {
        int n = 0;
        for (Collectible c : collectibles) {
            if (c.getCollectibleType() == Collectible.CollectibleType.OBSIDIAN) {
                n++;
            }
        }
        return n;
    }

    public String getName() {
        return name;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getSpawnLightX() {
        return spawnLightX;
    }

    public double getSpawnLightY() {
        return spawnLightY;
    }

    public double getSpawnShadowX() {
        return spawnShadowX;
    }

    public double getSpawnShadowY() {
        return spawnShadowY;
    }

    // ---------------------------------------------------------------
    // Construcción del mundo visual
    // ---------------------------------------------------------------

    /**
     * Agrega las vistas de todas las entidades al Group del mundo.
     * Se invoca una sola vez por nivel (los players y fondos se agregan aparte).
     */
    public Group buildWorld() {
        if (!worldBuilt) {
            addAllViews(platforms);
            addAllViews(movingPlatforms);
            addAllViews(ladders);
            addAllViews(hazards);
            addAllViews(collectibles);
            addAllViews(doors);
            addAllViews(checkpoints);
            addAllViews(switches);
            worldBuilt = true;
        }
        return worldGroup;
    }

    private void addAllViews(List<? extends com.luzysombra.entities.GameObject> entities) {
        for (var entity : entities) {
            worldGroup.getChildren().add(entity.getView());
        }
    }

    public Group getWorldGroup() {
        return worldGroup;
    }
}
