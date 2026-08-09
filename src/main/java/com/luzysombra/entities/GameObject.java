package com.luzysombra.entities;

import javafx.scene.Node;

/**
 * Base de todas las entidades del mundo del juego.
 * <p>
 * Las coordenadas (x, y) son coordenadas del MUNDO (no de la ventana). El Pane del
 * mundo se desplaza con la cámara; cada entidad tiene un {@link Node} de vista que se
 * posiciona con layoutX/layoutY en esas coordenadas del mundo.
 */
public abstract class GameObject {

    protected final String id;
    protected final String type;
    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected String imagePath;
    protected Node view;

    protected GameObject(String id, String type, double x, double y, double width, double height) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Crea (una sola vez) el nodo de vista de la entidad. */
    public abstract Node createView();

    public Node getView() {
        if (view == null) {
            view = createView();
            view.setLayoutX(x);
            view.setLayoutY(y);
        }
        return view;
    }

    /** Sincroniza la posición del nodo de vista con las coordenadas del mundo. */
    public void syncView() {
        if (view != null) {
            view.setLayoutX(x);
            view.setLayoutY(y);
        }
    }

    // ---------------------------------------------------------------
    // Accesores
    // ---------------------------------------------------------------

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public double getCenterX() {
        return x + width / 2.0;
    }

    public double getCenterY() {
        return y + height / 2.0;
    }

    public double getRight() {
        return x + width;
    }

    public double getBottom() {
        return y + height;
    }
}
