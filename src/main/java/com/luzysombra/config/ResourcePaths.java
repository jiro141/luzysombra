package com.luzysombra.config;

/**
 * Rutas de todos los recursos desde el classpath.
 * Todas las rutas comienzan con "/" y apuntan a archivos dentro de src/main/resources.
 * Si se reemplaza una imagen conservando el nombre, no hace falta tocar nada aquí.
 * Si se cambia el nombre de un archivo, actualizar la constante correspondiente.
 */
public final class ResourcePaths {

    private ResourcePaths() {
    }

    // ---------------------------------------------------------------
    // Hojas de estilo
    // ---------------------------------------------------------------
    public static final String STYLE_CSS = "/styles/game.css";

    // ---------------------------------------------------------------
    // Niveles
    // ---------------------------------------------------------------
    public static String levelJson(int levelNumber) {
        return "/levels/level-" + levelNumber + ".json";
    }

    // ---------------------------------------------------------------
    // Fondos
    // ---------------------------------------------------------------
    public static final String BACKGROUND_MENU = "/assets/images/backgrounds/background-menu.png";

    public static String backgroundLevel(int levelNumber) {
        return "/assets/images/backgrounds/background-level-" + levelNumber + ".png";
    }

    // ---------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------
    public static final String LOGO = "/assets/images/ui/logo-luz-sombra.png";
    public static final String PAUSE_ICON = "/assets/images/ui/pause-icon.png";
    public static final String ARROW_LEFT = "/assets/images/ui/arrow-left.png";
    public static final String ARROW_RIGHT = "/assets/images/ui/arrow-right.png";
    public static final String LOCK = "/assets/images/ui/lock.png";

    // ---------------------------------------------------------------
    // Personajes — Luz (imágenes numeradas: alta = de pie, baja = caer)
    // ---------------------------------------------------------------
    public static final String LIGHT_IDLE = "/assets/images/characters/light/28.png";
    public static final String LIGHT_WALK = "/assets/images/characters/light/21.png";
    public static final String LIGHT_RUN = "/assets/images/characters/light/22.png";
    public static final String LIGHT_JUMP = "/assets/images/characters/light/23.png";
    public static final String LIGHT_FALL = "/assets/images/characters/light/26.png";

    // ---------------------------------------------------------------
    // Personajes — Sombra (imágenes numeradas: alta = de pie, baja = caer)
    // ---------------------------------------------------------------
    public static final String SHADOW_IDLE = "/assets/images/characters/shadow/31.png";
    public static final String SHADOW_WALK = "/assets/images/characters/shadow/30.png";
    public static final String SHADOW_RUN = "/assets/images/characters/shadow/16.png";
    public static final String SHADOW_JUMP = "/assets/images/characters/shadow/14.png";
    public static final String SHADOW_FALL = "/assets/images/characters/shadow/18.png";

    // ---------------------------------------------------------------
    // Coleccionables
    // ---------------------------------------------------------------
    public static final String ALBOR = "/assets/images/collectibles/collectible-albor.png";
    public static final String OBSIDIAN = "/assets/images/collectibles/collectible-obsidian.png";

    // ---------------------------------------------------------------
    // Peligros
    // ---------------------------------------------------------------
    public static final String SPIKES = "/assets/images/hazards/spikes.png";
    public static final String HAZARD_LIGHT = "/assets/images/hazards/hazard-light.png";
    public static final String HAZARD_SHADOW = "/assets/images/hazards/hazard-shadow.png";

    // ---------------------------------------------------------------
    // Plataformas y elementos de nivel
    // ---------------------------------------------------------------
    public static final String PLATFORM = "/assets/images/platforms/platform.png";
    public static final String MOVING_PLATFORM = "/assets/images/platforms/moving-platform.png";
    public static final String LADDER = "/assets/images/platforms/ladder.png";

    // ---------------------------------------------------------------
    // Puertas
    // ---------------------------------------------------------------
    public static final String DOOR_LIGHT_CLOSED = "/assets/images/doors/door-light-closed.png";
    public static final String DOOR_LIGHT_OPEN = "/assets/images/doors/door-light-open.png";
    public static final String DOOR_SHADOW_CLOSED = "/assets/images/doors/door-shadow-closed.png";
    public static final String DOOR_SHADOW_OPEN = "/assets/images/doors/door-shadow-open.png";

    // ---------------------------------------------------------------
    // Miniaturas de niveles
    // ---------------------------------------------------------------
    public static String levelThumbnail(int levelNumber) {
        return "/assets/images/levels/level-thumbnail-" + levelNumber + ".png";
    }

    // ---------------------------------------------------------------
    // Audio
    // ---------------------------------------------------------------
    public static final String SOUND_CLICK = "/assets/sounds/click.wav";
    public static final String SOUND_JUMP = "/assets/sounds/jump.wav";
    public static final String SOUND_COLLECT_LIGHT = "/assets/sounds/collect-light.wav";
    public static final String SOUND_COLLECT_SHADOW = "/assets/sounds/collect-shadow.wav";
    public static final String SOUND_HURT = "/assets/sounds/hurt.wav";
    public static final String SOUND_DOOR = "/assets/sounds/door.wav";
    public static final String SOUND_VICTORY = "/assets/sounds/victory.wav";
    public static final String SOUND_GAME_OVER = "/assets/sounds/gameover.wav";
    public static final String SOUND_CHECKPOINT = "/assets/sounds/checkpoint.wav";
    public static final String MUSIC_AMBIENT = "/assets/music/ambient.wav";
}
