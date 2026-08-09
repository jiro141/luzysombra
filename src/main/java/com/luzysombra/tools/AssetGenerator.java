package com.luzysombra.tools;

import com.luzysombra.config.ResourcePaths;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Genera TODOS los PNG originales del juego en {@code src/main/resources/assets/images}
 * usando AWT puro (java.awt + javax.imageio), sin JavaFX (no está en el classpath
 * de herramientas; mismo enfoque que {@link AudioGenerator}).
 * <p>
 * Las rutas de salida se toman directamente de {@link ResourcePaths} para que
 * coincidan EXACTAMENTE (mayúsculas/plurales/extensiones) con lo que el juego espera.
 * <p>
 * Ejecutar con Maven:
 * {@code mvn compile exec:java -Dexec.mainClass=com.luzysombra.tools.AssetGenerator}
 */
public final class AssetGenerator {

    private static final Random RND = new Random(20260720L);

    private static final File RESOURCES_DIR = resolveResourcesDir();

    private static int generated = 0;
    private static int errors = 0;

    // ---------------------------------------------------------------
    // Paletas de cielo místicas
    // ---------------------------------------------------------------

    /** top/bottom = degradado de cielo; orb = sol/luna; mount = siluetas; stars = si hay estrellas. */
    private record Palette(Color top, Color bottom, Color orbCore, Color orbGlow,
                           Color mountFar, Color mountNear, boolean stars) {
    }

    private static final Palette MENU_PALETTE = new Palette(
            new Color(20, 15, 40), new Color(92, 62, 124),
            new Color(255, 232, 170), new Color(255, 240, 205),
            new Color(64, 48, 96), new Color(30, 24, 50), true);

    /** Paleta y ambiente DISTINTOS por nivel. Índice = nivel - 1. */
    private static final Palette[] LEVEL_PALETTES = {
            // 1 — El Atrio del Alba: amanecer, dorados, naranjas suaves, celeste pálido
            new Palette(new Color(150, 172, 205), new Color(255, 206, 128),
                    new Color(255, 246, 220), new Color(255, 234, 184),
                    new Color(188, 152, 132), new Color(120, 80, 62), false),
            // 2 — Puentes de Penumbra: penumbra violeta oscura, morados, grises, niebla
            new Palette(new Color(30, 24, 60), new Color(86, 68, 112),
                    new Color(206, 188, 250), new Color(162, 132, 216),
                    new Color(66, 54, 96), new Color(34, 28, 54), false),
            // 3 — El Santuario Sellado: atardecer místico, rojos y ámbar profundos, ruinas
            new Palette(new Color(92, 32, 44), new Color(216, 122, 60),
                    new Color(255, 200, 120), new Color(255, 160, 92),
                    new Color(110, 62, 52), new Color(58, 32, 30), false),
            // 4 — La Torre de la Medianoche: noche estrellada, azul noche, luna, torre
            new Palette(new Color(8, 16, 52), new Color(46, 66, 120),
                    new Color(240, 246, 255), new Color(195, 215, 255),
                    new Color(46, 56, 100), new Color(20, 24, 50), true),
            // 5 — El Umbral Eterno: eclipse, contraste máximo, anillo de luz, frío con dorado
            new Palette(new Color(6, 6, 14), new Color(46, 40, 66),
                    new Color(8, 8, 16), new Color(255, 215, 140),
                    new Color(40, 34, 58), new Color(12, 10, 20), true)
    };

    /** Paleta distinta por nivel para las miniaturas. */
    private static final Palette[] THUMB_PALETTES = {
            new Palette(new Color(108, 72, 84), new Color(226, 172, 128),
                    new Color(255, 230, 170), new Color(255, 242, 210),
                    new Color(150, 102, 98), new Color(78, 50, 60), false),
            new Palette(new Color(46, 38, 96), new Color(160, 104, 190),
                    new Color(215, 180, 255), new Color(225, 200, 255),
                    new Color(96, 66, 130), new Color(38, 30, 62), false),
            new Palette(new Color(84, 44, 72), new Color(206, 126, 118),
                    new Color(255, 214, 170), new Color(255, 230, 205),
                    new Color(140, 80, 90), new Color(64, 40, 56), false),
            new Palette(new Color(24, 52, 92), new Color(128, 200, 210),
                    new Color(235, 250, 255), new Color(245, 255, 255),
                    new Color(80, 110, 140), new Color(34, 44, 66), false),
            new Palette(new Color(16, 10, 34), new Color(78, 52, 118),
                    new Color(255, 238, 200), new Color(255, 245, 220),
                    new Color(52, 38, 80), new Color(22, 16, 40), true)
    };

    /**
     * Perfiles de montañas (fracción de la altura sobre la línea base).
     * El primer y último valor coinciden para que el fondo se pueda repetir
     * horizontalmente sin costuras (GameScreen repite el fondo 3 veces).
     */
    private static final double[] MENU_FAR = {0.06, 0.16, 0.11, 0.20, 0.15, 0.18, 0.12, 0.17, 0.06};
    private static final double[] MENU_NEAR = {0.04, 0.13, 0.08, 0.16, 0.11, 0.07, 0.14, 0.09, 0.04};

    /** Perfiles de cordillera por nivel (índice = nivel - 1). Primer y último valor coinciden: tileable. */
    private static final double[][] LEVEL_FAR_PROFILES = {
            {0.05, 0.16, 0.11, 0.20, 0.13, 0.18, 0.10, 0.15, 0.05},
            {0.06, 0.15, 0.10, 0.21, 0.14, 0.17, 0.11, 0.16, 0.06},
            {0.10, 0.18, 0.13, 0.22, 0.16, 0.20, 0.12, 0.17, 0.10},
            {0.08, 0.20, 0.14, 0.26, 0.17, 0.22, 0.12, 0.18, 0.08},
            {0.06, 0.17, 0.12, 0.23, 0.15, 0.19, 0.11, 0.16, 0.06}
    };

    private static final double[][] LEVEL_NEAR_PROFILES = {
            {0.03, 0.12, 0.07, 0.15, 0.10, 0.06, 0.13, 0.08, 0.03},
            {0.04, 0.13, 0.08, 0.16, 0.11, 0.07, 0.14, 0.09, 0.04},
            {0.05, 0.12, 0.08, 0.15, 0.11, 0.07, 0.13, 0.09, 0.05},
            {0.05, 0.16, 0.10, 0.19, 0.14, 0.08, 0.16, 0.11, 0.05},
            {0.04, 0.14, 0.09, 0.17, 0.12, 0.07, 0.15, 0.10, 0.04}
    };

    private static final double[][] THUMB_FAR = {
            {0.05, 0.14, 0.09, 0.18, 0.12, 0.08, 0.15, 0.10, 0.05},
            {0.06, 0.10, 0.20, 0.14, 0.08, 0.18, 0.12, 0.09, 0.06},
            {0.04, 0.12, 0.08, 0.15, 0.20, 0.13, 0.09, 0.12, 0.04},
            {0.08, 0.22, 0.16, 0.26, 0.18, 0.24, 0.14, 0.20, 0.08},
            {0.05, 0.15, 0.10, 0.13, 0.20, 0.16, 0.11, 0.14, 0.05}
    };

    private static final double[][] THUMB_NEAR = {
            {0.03, 0.12, 0.07, 0.15, 0.10, 0.06, 0.13, 0.08, 0.03},
            {0.04, 0.08, 0.16, 0.11, 0.06, 0.14, 0.10, 0.07, 0.04},
            {0.03, 0.10, 0.06, 0.12, 0.16, 0.10, 0.07, 0.10, 0.03},
            {0.06, 0.18, 0.13, 0.21, 0.15, 0.19, 0.11, 0.16, 0.06},
            {0.04, 0.12, 0.08, 0.10, 0.16, 0.13, 0.09, 0.11, 0.04}
    };

    private AssetGenerator() {
    }

    // ================================================================
    // Punto de entrada
    // ================================================================

    public static void main(String[] args) {
        generateLogo();
        generateBackgrounds();
        generateCharacters();
        generateCollectibles();
        generateHazards();
        generatePlatforms();
        generateDoors();
        generateUi();
        generateThumbnails();

        System.out.println("AssetGenerator: " + generated + " imágenes generadas correctamente"
                + (errors > 0 ? " (" + errors + " errores)" : ""));
    }

    // ================================================================
    // Generación por categoría
    // ================================================================

    private static void generateLogo() {
        write(create(600, 240, g -> AssetArt.drawLogoText(g, 600, 240)), ResourcePaths.LOGO);
    }

    private static void generateBackgrounds() {
        write(create(1600, 900, g -> paintScene(g, 1600, 900, MENU_PALETTE, MENU_FAR, MENU_NEAR, false)),
                ResourcePaths.BACKGROUND_MENU);
        // Un fondo por nivel. Si el PNG ya existe (p. ej. arte custom del usuario),
        // se conserva tal cual y el generador no lo pisa.
        for (int level = 1; level <= 5; level++) {
            int currentLevel = level;
            String path = ResourcePaths.backgroundLevel(level);
            File existing = new File(RESOURCES_DIR, path.startsWith("/") ? path.substring(1) : path);
            if (existing.exists()) {
                System.out.println("Conservado (ya existe): " + existing.getPath());
                continue;
            }
            write(create(1600, 900, g -> paintLevelBackground(g, 1600, 900, currentLevel)), path);
        }
    }

    /**
     * Genera los sprites de personajes SOLO si no existen (p. ej. arte custom
     * del usuario): se conserva el PNG existente y el generador no lo pisa.
     * Mismo criterio que los fondos. La pose "caer" reutiliza la de salto.
     */
    private static void generateCharacters() {
        writeIfMissing(create(80, 96, g -> AssetArt.drawLightCharacter(g, 80, 96, 0)), ResourcePaths.LIGHT_IDLE);
        writeIfMissing(create(80, 96, g -> AssetArt.drawLightCharacter(g, 80, 96, 1)), ResourcePaths.LIGHT_WALK);
        writeIfMissing(create(80, 96, g -> AssetArt.drawLightCharacter(g, 80, 96, 2)), ResourcePaths.LIGHT_RUN);
        writeIfMissing(create(80, 96, g -> AssetArt.drawLightCharacter(g, 80, 96, 3)), ResourcePaths.LIGHT_JUMP);
        writeIfMissing(create(80, 96, g -> AssetArt.drawLightCharacter(g, 80, 96, 3)), ResourcePaths.LIGHT_FALL);

        writeIfMissing(create(80, 96, g -> AssetArt.drawShadowCharacter(g, 80, 96, 0)), ResourcePaths.SHADOW_IDLE);
        writeIfMissing(create(80, 96, g -> AssetArt.drawShadowCharacter(g, 80, 96, 1)), ResourcePaths.SHADOW_WALK);
        writeIfMissing(create(80, 96, g -> AssetArt.drawShadowCharacter(g, 80, 96, 2)), ResourcePaths.SHADOW_RUN);
        writeIfMissing(create(80, 96, g -> AssetArt.drawShadowCharacter(g, 80, 96, 3)), ResourcePaths.SHADOW_JUMP);
        writeIfMissing(create(80, 96, g -> AssetArt.drawShadowCharacter(g, 80, 96, 3)), ResourcePaths.SHADOW_FALL);
    }

    /** Escribe la imagen solo si el archivo destino aún no existe (no pisa arte custom). */
    private static void writeIfMissing(BufferedImage img, String classpathPath) {
        String path = classpathPath.startsWith("/") ? classpathPath.substring(1) : classpathPath;
        File file = new File(RESOURCES_DIR, path);
        if (file.exists()) {
            System.out.println("Conservado (ya existe): " + file.getPath());
            return;
        }
        write(img, classpathPath);
    }

    private static void generateCollectibles() {
        write(create(48, 48, g -> AssetArt.drawAlbor(g, 48, 48)), ResourcePaths.ALBOR);
        write(create(48, 48, g -> AssetArt.drawObsidian(g, 48, 48)), ResourcePaths.OBSIDIAN);
    }

    private static void generateHazards() {
        write(create(320, 128, g -> AssetArt.drawPenumbra(g, 320, 128)), ResourcePaths.HAZARD_SHADOW);
        write(create(320, 128, g -> AssetArt.drawLuminosity(g, 320, 128)), ResourcePaths.HAZARD_LIGHT);
        write(create(128, 64, g -> AssetArt.drawSpikes(g, 128, 64)), ResourcePaths.SPIKES);
    }

    private static void generatePlatforms() {
        write(create(192, 48, g -> AssetArt.drawPlatform(g, 192, 48, false)), ResourcePaths.PLATFORM);
        write(create(192, 48, g -> AssetArt.drawPlatform(g, 192, 48, true)), ResourcePaths.MOVING_PLATFORM);
        write(create(48, 160, g -> AssetArt.drawLadder(g, 48, 160)), ResourcePaths.LADDER);
    }

    private static void generateDoors() {
        write(create(96, 192, g -> AssetArt.drawDoorLight(g, 96, 192, false)), ResourcePaths.DOOR_LIGHT_CLOSED);
        write(create(96, 192, g -> AssetArt.drawDoorLight(g, 96, 192, true)), ResourcePaths.DOOR_LIGHT_OPEN);
        write(create(96, 192, g -> AssetArt.drawDoorShadow(g, 96, 192, false)), ResourcePaths.DOOR_SHADOW_CLOSED);
        write(create(96, 192, g -> AssetArt.drawDoorShadow(g, 96, 192, true)), ResourcePaths.DOOR_SHADOW_OPEN);
    }

    private static void generateUi() {
        write(create(64, 64, g -> AssetArt.drawLock(g, 64, 64)), ResourcePaths.LOCK);
        write(create(48, 48, g -> AssetArt.drawPauseIcon(g, 48, 48)), ResourcePaths.PAUSE_ICON);
        write(create(48, 48, g -> AssetArt.drawArrow(g, 48, 48, false)), ResourcePaths.ARROW_LEFT);
        write(create(48, 48, g -> AssetArt.drawArrow(g, 48, 48, true)), ResourcePaths.ARROW_RIGHT);
    }

    private static void generateThumbnails() {
        for (int level = 1; level <= 5; level++) {
            Palette p = THUMB_PALETTES[level - 1];
            double[] far = THUMB_FAR[level - 1];
            double[] near = THUMB_NEAR[level - 1];
            write(create(320, 180, g -> paintScene(g, 320, 180, p, far, near, true)),
                    ResourcePaths.levelThumbnail(level));
        }
    }

    // ================================================================
    // Pintado de escenas (fondos y miniaturas)
    // ================================================================

    private static void paintScene(Graphics2D g, int w, int h, Palette p, double[] far, double[] near, boolean figures) {
        paintSkyAndOrb(g, w, h, p);

        g.setColor(p.mountFar());
        paintMountainRidge(g, w, h, h * 0.60, far);
        g.setColor(p.mountNear());
        paintMountainRidge(g, w, h, h * 0.80, near);

        if (figures) {
            paintFigures(g, w, h);
        }
    }

    /** Cielo en degradado + estrellas + sol/luna. */
    private static void paintSkyAndOrb(Graphics2D g, int w, int h, Palette p) {
        g.setPaint(new GradientPaint(0, 0, p.top(), 0, h, p.bottom()));
        g.fillRect(0, 0, w, h);

        if (p.stars()) {
            paintStars(g, w, h);
        }

        paintOrb(g, w, h, p);
    }

    /** Compone el fondo del nivel según su ambiente. */
    private static void paintLevelBackground(Graphics2D g, int w, int h, int level) {
        Palette p = LEVEL_PALETTES[level - 1];
        double[] far = LEVEL_FAR_PROFILES[level - 1];
        double[] near = LEVEL_NEAR_PROFILES[level - 1];
        switch (level) {
            case 1 -> paintScene(g, w, h, p, far, near, false);
            case 2 -> {
                paintScene(g, w, h, p, far, near, false);
                paintFog(g, w, h);
            }
            case 3 -> {
                paintSkyAndOrb(g, w, h, p);
                g.setColor(p.mountFar());
                paintRuinsRidge(g, w, h, h * 0.62, far);
                g.setColor(p.mountNear());
                paintRuinsRidge(g, w, h, h * 0.82, near);
            }
            case 4 -> {
                paintScene(g, w, h, p, far, near, false);
                paintTower(g, w, h, p.mountNear());
            }
            case 5 -> {
                paintEclipseSky(g, w, h, p);
                g.setColor(p.mountFar());
                paintMountainRidge(g, w, h, h * 0.60, far);
                g.setColor(p.mountNear());
                paintMountainRidge(g, w, h, h * 0.80, near);
            }
            default -> throw new IllegalArgumentException("Nivel de fondo desconocido: " + level);
        }
    }

    /** Capas de niebla translúcidas a todo el ancho (nivel 2). No rompe el tileado. */
    private static void paintFog(Graphics2D g, int w, int h) {
        for (int i = 0; i < 3; i++) {
            double y0 = h * (0.50 + i * 0.15);
            double bandH = 34 + i * 22;
            g.setPaint(new GradientPaint(0, (float) y0, new Color(148, 128, 186, 0),
                    0, (float) (y0 + bandH), new Color(148, 128, 186, 96)));
            g.fillRect(0, (int) y0, w, (int) bandH);
        }
    }

    /** Columnas de ruinas (nivel 3). El perfil empieza y termina a igual altura: tileable. */
    private static void paintRuinsRidge(Graphics2D g, int w, int h, double baseline, double[] heights) {
        int n = heights.length;
        double spacing = w / (double) n;
        double colW = spacing * 0.50;
        for (int i = 0; i < n; i++) {
            double cx = i * spacing + (spacing - colW) / 2;
            double colH = heights[i] * h;
            g.fill(new RoundRectangle2D.Double(cx, baseline - colH, colW, colH + 1, colW * 0.18, colW * 0.18));
            if (i % 4 == 1) {
                // Resto de dintel roto sobre la columna
                g.fill(new RoundRectangle2D.Double(cx - colW * 0.12, baseline - colH - colW * 0.30, colW * 0.60, colW * 0.30, 5, 5));
            }
        }
    }

    /** Silueta de torre con aguja y torrecillas (nivel 4). No toca los bordes: tileable. */
    private static void paintTower(Graphics2D g, int w, int h, Color color) {
        g.setColor(color);
        double baseY = h * 0.86;
        double cx = w * 0.48;
        double halfW = w * 0.045;
        double towerH = h * 0.40;
        Path2D tower = new Path2D.Double();
        tower.moveTo(cx - halfW, baseY);
        tower.lineTo(cx - halfW * 0.66, baseY - towerH);
        tower.lineTo(cx - halfW * 0.18, baseY - towerH - halfW * 0.7);
        tower.lineTo(cx + halfW * 0.18, baseY - towerH - halfW * 0.7);
        tower.lineTo(cx + halfW * 0.66, baseY - towerH);
        tower.lineTo(cx + halfW, baseY);
        tower.closePath();
        g.fill(tower);
        // Ventana iluminada
        g.setColor(new Color(255, 214, 130, 200));
        g.fill(new RoundRectangle2D.Double(cx - halfW * 0.13, baseY - towerH * 0.52, halfW * 0.26, halfW * 0.55, 3, 3));
        // Torrecillas laterales
        g.setColor(color);
        g.fill(new RoundRectangle2D.Double(cx - halfW * 2.4, baseY - h * 0.20, halfW * 0.9, h * 0.20, 4, 4));
        g.fill(new RoundRectangle2D.Double(cx + halfW * 1.5, baseY - h * 0.16, halfW * 0.9, h * 0.16, 4, 4));
    }

    /** Cielo del eclipse: degradado frío + estrellas + disco oscuro con anillo de luz (nivel 5). */
    private static void paintEclipseSky(Graphics2D g, int w, int h, Palette p) {
        g.setPaint(new GradientPaint(0, 0, p.top(), 0, h, p.bottom()));
        g.fillRect(0, 0, w, h);

        if (p.stars()) {
            paintStars(g, w, h);
        }

        int x = (int) (w * 0.5);
        int y = (int) (h * 0.30);
        int r = Math.max(6, (int) (h * 0.10));
        // Halo dorado difuso
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g.setPaint(new RadialGradientPaint(x, y, r * 2.2f,
                new float[]{0f, 0.4f, 0.8f, 1f},
                new Color[]{p.orbGlow(), new Color(255, 215, 140, 110), new Color(255, 215, 140, 25), new Color(0, 0, 0, 0)}));
        g.fill(new Ellipse2D.Double(x - r * 2.2, y - r * 2.2, r * 4.4, r * 4.4));
        g.setComposite(AlphaComposite.SrcOver);
        // Anillo de luz brillante
        g.setColor(p.orbGlow());
        g.fillOval(x - r, y - r, r * 2, r * 2);
        // Disco oscuro que lo oculta (luna eclipsando)
        g.setColor(new Color(6, 6, 14));
        g.fillOval(x - (int) (r * 0.80), y - (int) (r * 0.80), (int) (r * 1.60), (int) (r * 1.60));
    }

    /** Sol/luna con halo radial. */
    private static void paintOrb(Graphics2D g, int w, int h, Palette p) {
        int x = (int) (w * 0.72);
        int y = (int) (h * 0.28);
        int r = Math.max(6, (int) (h * 0.13));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g.setPaint(new RadialGradientPaint(x, y, r * 1.7f,
                new float[]{0f, 0.45f, 1f},
                new Color[]{p.orbCore(), p.orbGlow(), new Color(0, 0, 0, 0)}));
        g.fill(new Ellipse2D.Double(x - r * 1.7, y - r * 1.7, r * 3.4, r * 3.4));
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(p.orbCore());
        g.fillOval(x - r / 2, y - r / 2, r, r);
    }

    private static void paintStars(Graphics2D g, int w, int h) {
        g.setColor(new Color(255, 244, 220, 190));
        int count = Math.max(20, (w * h) / 9000);
        for (int i = 0; i < count; i++) {
            int x = 8 + RND.nextInt(Math.max(1, w - 16));
            int y = RND.nextInt(Math.max(1, (int) (h * 0.45)));
            int s = 1 + RND.nextInt(2);
            g.fillOval(x, y, s, s);
        }
    }

    /**
     * Cordillera de siluetas. El perfil empieza y termina a la misma altura para
     * que el fondo se pueda repetir horizontalmente sin costuras.
     */
    private static void paintMountainRidge(Graphics2D g, int w, int h, double baseline, double[] profile) {
        Path2D path = new Path2D.Double();
        path.moveTo(0, h);
        path.lineTo(0, baseline);
        int n = profile.length;
        for (int i = 0; i < n; i++) {
            double x = (double) i / (n - 1) * w;
            path.lineTo(x, baseline - profile[i] * h);
        }
        path.lineTo(w, baseline);
        path.lineTo(w, h);
        path.closePath();
        g.fill(path);
    }

    /** Siluetas de Luz y Sombra enfrentadas (solo en miniaturas). */
    private static void paintFigures(Graphics2D g, int w, int h) {
        int base = (int) (h * 0.88);
        int lightX = (int) (w * 0.38);
        int shadowX = (int) (w * 0.62);

        g.setColor(new Color(250, 246, 232));
        g.fillOval(lightX - 7, base - 22, 12, 12);
        g.fill(new RoundRectangle2D.Double(lightX - 9, base - 12, 16, 18, 5, 5));

        g.setColor(new Color(28, 24, 40));
        g.fillOval(shadowX - 6, base - 22, 12, 12);
        g.fill(new RoundRectangle2D.Double(shadowX - 8, base - 12, 16, 18, 5, 5));
    }

    // ================================================================
    // Utilidades
    // ================================================================

    /** Crea un BufferedImage transparente y le aplica un painter sobre Graphics2D preparado. */
    private static BufferedImage create(int w, int h, Consumer<Graphics2D> painter) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        AssetArt.setup(g);
        painter.accept(g);
        g.dispose();
        return img;
    }

    /** Resuelve el directorio raíz de resources (working dir de Maven o user.dir). */
    private static File resolveResourcesDir() {
        File dir = new File("src/main/resources");
        if (dir.isDirectory()) {
            return dir;
        }
        return new File(System.getProperty("user.dir"), "src/main/resources");
    }

    /** Escribe la imagen como PNG bajo src/main/resources, creando directorios padre. */
    private static void write(BufferedImage img, String classpathPath) {
        String path = classpathPath.startsWith("/") ? classpathPath.substring(1) : classpathPath;
        File file = new File(RESOURCES_DIR, path);
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            errors++;
            System.err.println("No se pudieron crear directorios para: " + file.getPath());
            return;
        }
        try {
            if (ImageIO.write(img, "PNG", file)) {
                generated++;
                System.out.println("Generado: " + file.getPath());
            } else {
                errors++;
                System.err.println("No se pudo escribir la imagen: " + file.getPath());
            }
        } catch (IOException ex) {
            errors++;
            System.err.println("Error generando " + file.getPath() + ": " + ex.getMessage());
        }
    }
}
