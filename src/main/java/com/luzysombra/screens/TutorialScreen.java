package com.luzysombra.screens;

import com.luzysombra.config.GameConfig;
import com.luzysombra.config.ResourcePaths;
import com.luzysombra.resources.ResourceManager;
import com.luzysombra.ui.GameButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Tutorial del juego en tres páginas internas (sin ventanas nuevas):
 * <ol>
 *   <li>Controles de Luz y Sombra + descripción de los personajes.</li>
 *   <li>Albores, Obsidianas y objetivos de recolección.</li>
 *   <li>Peligros (penumbra, luminosidad, púas) y condición de victoria.</li>
 * </ol>
 */
public class TutorialScreen extends StackPane {

    private final StackPane pageContainer = new StackPane();
    private final Label pageIndicator = new Label("1 / 3");
    private final GameButton prevButton;
    private final GameButton nextButton;
    private final Runnable onStart;
    private Runnable currentNextAction;
    private int currentPage = 0;

    public TutorialScreen(Runnable onBackToMenu, Runnable onStart) {
        this.onStart = onStart;
        setPrefSize(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        getStyleClass().add("tutorial-screen");

        Rectangle background = new Rectangle(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        background.setFill(Color.rgb(24, 18, 12, 0.92));
        getChildren().add(background);

        VBox root = new VBox(22);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(34, 60, 34, 60));

        Label title = new Label("TUTORIAL");
        title.getStyleClass().add("screen-title");

        pageContainer.setPrefSize(1300, 560);

        // Navegación inferior
        HBox nav = new HBox(24);
        nav.setAlignment(Pos.CENTER);

        prevButton = new GameButton("ANTERIOR", GameButton.Variant.GHOST, this::previousPage);
        nextButton = new GameButton("SIGUIENTE", GameButton.Variant.GHOST,
                () -> {
                    if (currentNextAction != null) {
                        currentNextAction.run();
                    }
                });
        GameButton back = new GameButton("VOLVER AL MENÚ", GameButton.Variant.DANGER, onBackToMenu);

        pageIndicator.getStyleClass().add("page-indicator");

        nav.getChildren().addAll(prevButton, pageIndicator, nextButton, back);

        root.getChildren().addAll(title, pageContainer, nav);
        getChildren().add(root);

        buildPages();
        showPage(0);
    }

    private void buildPages() {
        pageContainer.getChildren().addAll(
                page1(),
                page2(),
                page3()
        );
    }

    private void showPage(int index) {
        currentPage = Math.max(0, Math.min(2, index));
        for (int i = 0; i < pageContainer.getChildren().size(); i++) {
            pageContainer.getChildren().get(i).setVisible(i == currentPage);
        }
        pageIndicator.setText((currentPage + 1) + " / 3");
        prevButton.setDisable(currentPage == 0);

        if (currentPage == 2) {
            // Última página: SIGUIENTE pasa a ser COMENZAR y arranca el juego
            nextButton.setText("COMENZAR");
            nextButton.getStyleClass().add("tutorial-start");
            nextButton.setDisable(false);
            currentNextAction = onStart;
        } else {
            nextButton.setText("SIGUIENTE");
            nextButton.getStyleClass().remove("tutorial-start");
            nextButton.setDisable(false);
            currentNextAction = this::nextPage;
        }
    }

    private void nextPage() {
        showPage(currentPage + 1);
    }

    private void previousPage() {
        showPage(currentPage - 1);
    }

    // ================================================================
    // Página 1: personajes y controles
    // ================================================================

    private VBox page1() {
        VBox page = new VBox(24);
        page.setAlignment(Pos.CENTER);

        HBox cards = new HBox(30);
        cards.setAlignment(Pos.CENTER);

        // Luz
        VBox lightCard = card(
                "LUZ",
                "Ser de luz blanca y dorada. Recoge Albores y camina por zonas normales y luminosas. "
                        + "Evita las penumbras y las púas. Llega a la puerta blanca.",
                "hud-light");

        VBox lightKeys = new VBox(8, keyRow("A", "Izquierda"), keyRow("D", "Derecha"),
                keyRow("W", "Saltar / Subir"), keyRow("S", "Bajar escalera"));
        lightKeys.setAlignment(Pos.CENTER);
        lightCard.getChildren().add(lightKeys);

        // Sombra
        VBox shadowCard = card(
                "SOMBRA",
                "Criatura de la penumbra. Recoge Obsidianas y camina por zonas normales y oscuras. "
                        + "Evita las luminosidades y las púas. Llega a la puerta negra.",
                "hud-shadow");

        VBox shadowKeys = new VBox(8, keyRow("J", "Izquierda"), keyRow("L", "Derecha"),
                keyRow("I", "Saltar / Subir"), keyRow("K", "Bajar escalera"));
        shadowKeys.setAlignment(Pos.CENTER);
        shadowCard.getChildren().add(shadowKeys);

        cards.getChildren().addAll(lightCard, shadowCard);

        Label tip = new Label("Ambos jugadores comparten el teclado y se mueven al mismo tiempo. "
                + "ESC pausa el juego.");
        tip.getStyleClass().add("tutorial-tip");
        page.getChildren().addAll(cards, tip);
        return page;
    }

    // ================================================================
    // Página 2: coleccionables
    // ================================================================

    private VBox page2() {
        VBox page = new VBox(24);
        page.setAlignment(Pos.CENTER);

        HBox cards = new HBox(30);
        cards.setAlignment(Pos.CENTER);

        VBox alborCard = card("ALBOR",
                "Llama de luz blanca. Solo Luz puede recogerla.",
                "hud-albor");
        alborCard.getChildren().add(0, icon(ResourcePaths.ALBOR));

        VBox obsidianCard = card("OBSIDIANA",
                "Cristal negro con reflejos violetas. Solo Sombra puede recogerla.",
                "hud-obsidian");
        obsidianCard.getChildren().add(0, icon(ResourcePaths.OBSIDIAN));

        cards.getChildren().addAll(alborCard, obsidianCard);

        Label objective = new Label("Objetivo: recoger TODOS los Albores con Luz y TODAS las "
                + "Obsidianas con Sombra.\nSolo entonces se iluminarán las puertas de salida.");
        objective.getStyleClass().add("tutorial-tip");
        objective.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        page.getChildren().addAll(cards, objective);
        return page;
    }

    // ================================================================
    // Página 3: peligros y victoria
    // ================================================================

    private VBox page3() {
        VBox page = new VBox(20);
        page.setAlignment(Pos.CENTER);

        HBox cards = new HBox(26);
        cards.setAlignment(Pos.CENTER);

        VBox penumbra = card("PENUMBRA",
                "Zona de sombra espesa: daña SOLO a Luz.",
                "hud-shadow");
        penumbra.getChildren().add(0, icon(ResourcePaths.HAZARD_SHADOW));

        VBox luminosity = card("LUMINOSIDAD",
                "Zona de luz intensa: daña SOLO a Sombra.",
                "hud-light");
        luminosity.getChildren().add(0, icon(ResourcePaths.HAZARD_LIGHT));

        VBox spikes = card("PÚAS",
                "Dañan a ambos personajes.",
                "hud-caption");
        spikes.getChildren().add(0, icon(ResourcePaths.SPIKES));

        cards.getChildren().addAll(penumbra, luminosity, spikes);

        Label victory = new Label("VICTORIA: Luz en su puerta blanca y Sombra en su puerta negra, "
                + "habiendo recogido todos los coleccionables.\n"
                + "Tras recibir daño, el personaje parpadea y es invulnerable por un momento; "
                + "reaparece en su último punto seguro.");
        victory.getStyleClass().add("tutorial-tip");
        victory.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        page.getChildren().addAll(cards, victory);
        return page;
    }

    // ================================================================
    // Utilidades de construcción
    // ================================================================

    private VBox card(String titleText, String description, String accentClass) {
        VBox card = new VBox(12);
        card.setPrefWidth(380);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("tutorial-card");

        Label title = new Label(titleText);
        title.getStyleClass().add("tutorial-card-title");
        title.getStyleClass().add(accentClass);

        Label desc = new Label(description);
        desc.getStyleClass().add("tutorial-card-text");
        desc.setWrapText(true);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(title, desc);
        return card;
    }

    private HBox keyRow(String key, String action) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        Label keyCap = new Label(key);
        keyCap.getStyleClass().add("key-cap");
        Label actionLabel = new Label(action);
        actionLabel.getStyleClass().add("tutorial-card-text");
        row.getChildren().addAll(keyCap, actionLabel);
        return row;
    }

    private ImageView icon(String path) {
        ImageView icon = new ImageView(ResourceManager.getInstance().getImage(path));
        icon.setFitWidth(56);
        icon.setFitHeight(56);
        icon.setPreserveRatio(true);
        return icon;
    }
}
