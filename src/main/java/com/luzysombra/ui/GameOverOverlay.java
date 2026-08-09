package com.luzysombra.ui;

import javafx.scene.control.Label;

/**
 * Overlay de derrota: se muestra cuando ambos personajes agotan sus vidas.
 * Incluye la causa y opciones para reintentar, volver a la selección de
 * niveles o regresar al menú principal. Todo en la misma ventana.
 */
public class GameOverOverlay extends GameOverlay {

    private final Label causeLabel = title("", "overlay-caption");

    public GameOverOverlay(Runnable onRetry, Runnable onLevelSelect, Runnable onMenu) {
        Label title = title("FIN DEL JUEGO", "overlay-title");
        Label subtitle = title("La luz y la sombra se han extinguido", "overlay-subtitle");
        causeLabel.setText("Los personajes se quedaron sin vidas.");

        GameButton retry = new GameButton("REINTENTAR", GameButton.Variant.PRIMARY, onRetry);
        GameButton levels = new GameButton("SELECCIÓN DE NIVELES", GameButton.Variant.GHOST, onLevelSelect);
        GameButton menu = new GameButton("MENÚ PRINCIPAL", GameButton.Variant.DANGER, onMenu);

        retry.setMaxWidth(300);
        levels.setMaxWidth(300);
        menu.setMaxWidth(300);
        add(title, subtitle, causeLabel, retry, levels, menu);
    }

    /** Personaliza la causa de la derrota. */
    public void setCause(String cause) {
        causeLabel.setText(cause);
    }
}
