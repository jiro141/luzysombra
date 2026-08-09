package com.luzysombra.entities;

import com.luzysombra.config.GameConfig;
import com.luzysombra.input.InputManager;
import com.luzysombra.resources.ResourceManager;
import javafx.scene.input.KeyCode;

/**
 * Personaje jugable abstracto. Contiene la lógica de movimiento, gravedad,
 * salto (con coyote time y jump buffer para buena jugabilidad), escaleras,
 * vidas, invulnerabilidad y respawn en el último punto seguro.
 * <p>
 * Luz y Sombra comparten toda esta lógica; solo cambian sus teclas y su estética.
 */
public abstract class Player extends GameObject {

    protected double vx;
    protected double vy;
    protected boolean grounded;
    protected boolean onLadder;
    /** Dirección horizontal deseada: -1 (izq), 0 (quieto), +1 (der). */
    protected int moveDir;
    protected int facing = 1;
    protected int lives = GameConfig.STARTING_LIVES;

    protected double invulnerableTimer;
    protected double hitTimer;
    protected double jumpBufferTimer;
    protected double coyoteTimer;
    protected boolean arrivedAtDoor;

    protected double respawnX;
    protected double respawnY;
    protected double levelWidth;
    protected double levelHeight;

    protected PlayerView playerView;

    protected Player(String id, String type, double x, double y) {
        super(id, type, x, y, GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT);
        this.x = x - width / 2.0;
        this.y = y - height;
        this.respawnX = this.x;
        this.respawnY = this.y;
    }

    // ---------------------------------------------------------------
    // Teclas de cada personaje
    // ---------------------------------------------------------------

    protected abstract KeyCode keyLeft();

    protected abstract KeyCode keyRight();

    protected abstract KeyCode keyJump();

    protected abstract KeyCode keyClimbUp();

    protected abstract KeyCode keyClimbDown();

    // ---------------------------------------------------------------
    // Configuración
    // ---------------------------------------------------------------

    public void setLevelBounds(double levelWidth, double levelHeight) {
        this.levelWidth = levelWidth;
        this.levelHeight = levelHeight;
    }

    public void setRespawn(double x, double y) {
        this.respawnX = x - width / 2.0;
        this.respawnY = y - height;
    }

    public void setOnLadder(boolean onLadder) {
        this.onLadder = onLadder;
    }

    // ---------------------------------------------------------------
    // Entrada (se llama una vez por frame, antes de la física)
    // ---------------------------------------------------------------

    public void handleInput(InputManager input) {
        int dir = 0;
        if (input.isDown(keyLeft())) {
            dir -= 1;
        }
        if (input.isDown(keyRight())) {
            dir += 1;
        }
        // Solo se registra la dirección deseada; la velocidad se acumula en updatePhysics
        moveDir = dir;
        if (dir != 0) {
            facing = dir;
        }

        // Buffer de salto: si presionas justo antes de aterrizar, el salto se encola
        if (input.isEdgePressed(keyJump())) {
            jumpBufferTimer = 0.12;
        }

        if (onLadder) {
            // En la escalera: las teclas verticales suben/bajan; la gravedad queda anulada
            if (input.isDown(keyClimbUp())) {
                vy = -GameConfig.LADDER_CLIMB_SPEED;
            } else if (input.isDown(keyClimbDown())) {
                vy = GameConfig.LADDER_CLIMB_SPEED;
            } else {
                vy = 0;
            }
        } else if (grounded || coyoteTimer > 0) {
            if (jumpBufferTimer > 0) {
                vy = -GameConfig.JUMP_SPEED;
                grounded = false;
                coyoteTimer = 0;
                jumpBufferTimer = 0;
                ResourceManager.getInstance().playSound(com.luzysombra.config.ResourcePaths.SOUND_JUMP);
            }
        }
    }

    // ---------------------------------------------------------------
    // Física (gravedad y timers; la integración de posición por ejes la
    // hace CollisionManager para poder resolver colisiones correctamente)
    // ---------------------------------------------------------------

    public void updatePhysics(double dt) {
        // Timers
        invulnerableTimer = Math.max(0, invulnerableTimer - dt);
        hitTimer = Math.max(0, hitTimer - dt);
        coyoteTimer = grounded ? 0.1 : Math.max(0, coyoteTimer - dt);
        jumpBufferTimer = Math.max(0, jumpBufferTimer - dt);

        // Aceleración horizontal (acumulativa): caminar → correr según |vx|
        double accel = GameConfig.ACCELERATION * (grounded ? 1.0 : GameConfig.AIR_CONTROL);
        if (moveDir != 0) {
            vx += moveDir * accel * dt;
            vx = Math.max(-GameConfig.MOVE_SPEED, Math.min(GameConfig.MOVE_SPEED, vx));
        } else if (grounded) {
            // Fricción al soltar la tecla (solo en el suelo)
            if (vx > 0) {
                vx = Math.max(0, vx - GameConfig.FRICTION * dt);
            } else if (vx < 0) {
                vx = Math.min(0, vx + GameConfig.FRICTION * dt);
            }
        }

        if (!onLadder) {
            vy += GameConfig.GRAVITY * dt;
            if (vy > GameConfig.MAX_FALL_SPEED) {
                vy = GameConfig.MAX_FALL_SPEED;
            }
        }
    }

    // ---------------------------------------------------------------
    // Daño y respawn
    // ---------------------------------------------------------------

    /**
     * Aplica daño si el personaje no está invulnerable.
     * Devuelve true si el personaje murió (vidas agotadas).
     */
    public boolean takeDamage() {
        if (invulnerableTimer > 0 || hitTimer > 0) {
            return false;
        }
        lives--;
        ResourceManager.getInstance().playSound(com.luzysombra.config.ResourcePaths.SOUND_HURT);
        invulnerableTimer = GameConfig.INVULNERABILITY_DURATION;
        hitTimer = GameConfig.HIT_STUN_DURATION;
        vx = 0;
        vy = 0;
        x = respawnX;
        y = respawnY;
        syncView();
        return lives <= 0;
    }

    // ---------------------------------------------------------------
    // Consultas de estado
    // ---------------------------------------------------------------

    public int getLives() {
        return lives;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isOnLadder() {
        return onLadder;
    }

    public boolean isInvulnerable() {
        return invulnerableTimer > 0;
    }

    public boolean isInHitStun() {
        return hitTimer > 0;
    }

    public boolean hasArrivedAtDoor() {
        return arrivedAtDoor;
    }

    public void setArrivedAtDoor(boolean arrivedAtDoor) {
        this.arrivedAtDoor = arrivedAtDoor;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    // ---------------------------------------------------------------
    // Vista
    // ---------------------------------------------------------------

    @Override
    public javafx.scene.Node createView() {
        return playerView.getNode();
    }

    /** Actualiza la animación visual del personaje. */
    public void updateVisual(double delta) {
        boolean moving = Math.abs(vx) > 1;
        boolean airborne = !grounded && !onLadder;
        playerView.update(delta, moving, airborne, vx, vy, onLadder,
                isInHitStun(), invulnerableTimer, facing);
        syncView();
    }
}
