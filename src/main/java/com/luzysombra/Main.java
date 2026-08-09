package com.luzysombra;

import javafx.application.Application;

/**
 * Punto de entrada del juego.
 * <p>
 * IMPORTANTE: esta clase NO extiende {@link Application} a propósito: lanza la
 * aplicación JavaFX de forma estándar, lo que permite ejecutar el JAR generado
 * con {@code java -jar} sin problemas de "JavaFX runtime components are missing".
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Application.launch(GameApplication.class, args);
    }
}
