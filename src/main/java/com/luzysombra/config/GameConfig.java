package com.luzysombra.config;

/**
 * Constantes globales del juego: resolución lógica, física, temporizadores y valores de diseño.
 * El mundo del juego usa coordenadas lógicas de 1600x900; el contenedor de escala se encarga
 * de adaptarlas a cualquier tamaño de ventana sin deformar la relación 16:9.
 */
public final class GameConfig {

    private GameConfig() {
    }

    // ---------------------------------------------------------------
    // Resolución lógica y ventana
    // ---------------------------------------------------------------
    public static final double LOGICAL_WIDTH = 1600.0;
    public static final double LOGICAL_HEIGHT = 900.0;
    public static final double WINDOW_INITIAL_WIDTH = 1280.0;
    public static final double WINDOW_INITIAL_HEIGHT = 720.0;
    public static final double WINDOW_MIN_WIDTH = 960.0;
    public static final double WINDOW_MIN_HEIGHT = 540.0;

    // ---------------------------------------------------------------
    // Física
    // ---------------------------------------------------------------
    public static final double GRAVITY = 2300.0;
    /** Velocidad máxima horizontal del personaje (con aceleración gradual). */
    public static final double MOVE_SPEED = 420.0;
    /** Aceleración horizontal hacia MOVE_SPEED (px/s²) sobre el suelo. */
    public static final double ACCELERATION = 1600.0;
    /** Desaceleración horizontal al soltar la tecla (px/s²). */
    public static final double FRICTION = 1200.0;
    /** Umbral de |vx|: por debajo se muestra "caminar", por encima "correr". */
    public static final double WALK_SPEED = 210.0;
    /** Factor de aceleración en el aire (control de salto) respecto al suelo. */
    public static final double AIR_CONTROL = 0.7;
    public static final double JUMP_SPEED = 830.0;
    public static final double MAX_FALL_SPEED = 1400.0;
    public static final double LADDER_CLIMB_SPEED = 300.0;
    /** Delta máximo por frame para evitar saltos físicos al recuperar foco. */
    public static final double MAX_DELTA_TIME = 1.0 / 30.0;
    /** Duración de la invulnerabilidad tras recibir daño (segundos). */
    public static final double INVULNERABILITY_DURATION = 1.0;
    /** Tiempo que el personaje se mantiene en estado de impacto. */
    public static final double HIT_STUN_DURATION = 0.35;

    // ---------------------------------------------------------------
    // Personajes
    // ---------------------------------------------------------------
    public static final double PLAYER_WIDTH = 46.0;
    public static final double PLAYER_HEIGHT = 72.0;
    public static final int STARTING_LIVES = 3;

    // ---------------------------------------------------------------
    // Cámara
    // ---------------------------------------------------------------
    public static final double CAMERA_SMOOTHING = 8.0;

    // ---------------------------------------------------------------
    // Coleccionables
    // ---------------------------------------------------------------
    public static final double COLLECTIBLE_SIZE = 34.0;

    // ---------------------------------------------------------------
    // Carga de recursos
    // ---------------------------------------------------------------
    /** Duración mínima de la pantalla de carga (segundos). */
    public static final double LOADING_DURATION = 1.6;

    // ---------------------------------------------------------------
    // Niveles
    // ---------------------------------------------------------------
    public static final int TOTAL_LEVELS = 5;
    public static final String[] LEVEL_NAMES = {
            "Nivel 1: Primeros Pasos",
            "Nivel 2: Puentes en Movimiento",
            "Nivel 3: Esfuerzo Compartido",
            "Nivel 4: Las Alturas",
            "Nivel 5: El Último Ritual"
    };
}
