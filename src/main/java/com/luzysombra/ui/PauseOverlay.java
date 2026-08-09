package com.luzysombra.ui;

import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Overlay de pausa: se muestra dentro de la misma ventana, oscurece el
 * escenario y bloquea los controles del juego. ESC o el botón REANUDAR
 * vuelven al juego; el foco del teclado regresa al nivel al cerrar.
 */
public class PauseOverlay extends GameOverlay {

    private final Runnable onResume;
    private final Runnable onRestart;
    private final Runnable onMenu;

    public PauseOverlay(Runnable onResume, Runnable onRestart, Runnable onMenu) {
        this.onResume = onResume;
        this.onRestart = onRestart;
        this.onMenu = onMenu;

        Label title = title("PAUSA", "overlay-title");
        Label subtitle = title("El ritual se detiene…", "overlay-subtitle");

        GameButton resume = new GameButton("REANUDAR", GameButton.Variant.PRIMARY, onResume);
        GameButton restart = new GameButton("REINICIAR NIVEL", GameButton.Variant.GHOST, onRestart);
        GameButton menu = new GameButton("VOLVER AL MENÚ", GameButton.Variant.DANGER, onMenu);

        resume.setMaxWidth(300);
        restart.setMaxWidth(300);
        menu.setMaxWidth(300);
        add(title, subtitle, resume, restart, menu);

        // Captura de teclado: ESC reanuda (se consume para que no actúe en el juego)
        setOnKeyPressed(this::handleKey);
    }

    private void handleKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            event.consume();
            onResume.run();
        }
    }
}
