package com.luzysombra.ui;

import javafx.scene.control.Label;

/**
 * Confirmación interna de salida: se muestra DENTRO de la misma ventana
 * (overlay en el overlayLayer), nunca como diálogo nativo. "SÍ, SALIR" cierra
 * el juego; "CANCELAR" vuelve a la pantalla anterior.
 */
public class ConfirmExitOverlay extends GameOverlay {

    public ConfirmExitOverlay(Runnable onConfirm, Runnable onCancel) {
        Label title = title("¿SEGURO QUE QUERÉS SALIR?", "overlay-title");
        Label subtitle = title("Tu progreso se guarda automáticamente", "overlay-subtitle");

        GameButton yes = new GameButton("SÍ, SALIR", GameButton.Variant.DANGER, onConfirm);
        GameButton cancel = new GameButton("CANCELAR", GameButton.Variant.GHOST, onCancel);

        yes.setMaxWidth(300);
        cancel.setMaxWidth(300);
        add(title, subtitle, yes, cancel);
    }
}
