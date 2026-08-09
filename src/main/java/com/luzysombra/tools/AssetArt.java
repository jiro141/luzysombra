package com.luzysombra.tools;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Dibujo procedural (AWT puro, sin librerías externas) de todos los sprites
 * del juego con una estética única: minimalista, mística, luz contra sombra.
 * Todas las figuras se dibujan con fondo transparente.
 */
final class AssetArt {

    private static final Color LIGHT_BODY = new Color(250, 246, 232);
    private static final Color LIGHT_VEIL = new Color(245, 236, 216);
    private static final Color LIGHT_CROWN = new Color(255, 224, 150);
    private static final Color SHADOW_BODY = new Color(28, 24, 40);
    private static final Color SHADOW_HOOD = new Color(46, 40, 62);
    private static final Color SHADOW_EYE = new Color(178, 120, 255);
    private static final Color GOLD = new Color(216, 184, 120);
    private static final Color DARK_BROWN = new Color(58, 42, 26);
    private static final Color PARCHMENT = new Color(245, 236, 216);

    private AssetArt() {
    }

    static void setup(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ================================================================
    // Personajes
    // ================================================================

    /** Luz: espíritu de luz. pose: 0=idle, 1/2=run, 3=jump. */
    static void drawLightCharacter(Graphics2D g, int w, int h, int pose) {
        double cx = w / 2.0;
        double baseY = h - 12;

        // Aura dorada difusa
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g.setPaint(new RadialGradientPaint(
                (float) cx, (float) (baseY - 50), 64,
                new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(255, 224, 150, 200), new Color(255, 240, 200, 60), new Color(255, 240, 200, 0)}));
        g.fill(new Ellipse2D.Double(cx - 60, baseY - 106, 120, 120));
        g.setComposite(AlphaComposite.SrcOver);

        double lean = pose == 3 ? 0.06 : (pose == 1 ? -0.04 : (pose == 2 ? 0.04 : 0.0));
        double bob = pose == 0 ? Math.sin(System.nanoTime() / 1e9 * 2) * 0 : 0;

        // Velo / túnica
        Path2D veil = new Path2D.Double();
        veil.moveTo(cx - 16, baseY - 26);
        veil.curveTo(cx - 22, baseY - 8, cx - 12, baseY, cx - 5, baseY - 2);
        veil.curveTo(cx, baseY + (pose == 3 ? 2 : 0), cx + 8, baseY - 2, cx + 16, baseY - 26);
        veil.closePath();
        g.setColor(LIGHT_VEIL);
        g.fill(veil);

        // Cuerpo (inclinado según la pose)
        g.translate(cx, baseY - 30);
        g.rotate(lean);
        g.setColor(LIGHT_BODY);
        g.fill(new RoundRectangle2D.Double(-10, -20, 20, 34, 9, 9));
        // Brazo que oscila al correr
        double arm = pose == 1 ? -6 : (pose == 2 ? 6 : 0);
        g.setColor(new Color(240, 232, 212));
        g.fill(new RoundRectangle2D.Double(-12, -14 + arm * 0.3, 5, 12, 3, 3));
        g.fill(new RoundRectangle2D.Double(7, -14 - arm * 0.3, 5, 12, 3, 3));
        g.rotate(-lean);
        g.translate(-cx, -(baseY - 30));

        // Cabeza con resplandor
        g.setColor(new Color(255, 252, 240));
        g.fillOval((int) (cx - 8), (int) (baseY - 52), 16, 16);

        // Corona de luz
        g.setColor(LIGHT_CROWN);
        g.fillOval((int) (cx - 4), (int) (baseY - 60), 8, 6);

        g.dispose();
    }

    /** Sombra: criatura de la penumbra. pose: 0=idle, 1/2=run, 3=jump. */
    static void drawShadowCharacter(Graphics2D g, int w, int h, int pose) {
        double cx = w / 2.0;
        double baseY = h - 12;

        // Aura de humo violeta oscuro
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g.setPaint(new RadialGradientPaint(
                (float) cx, (float) (baseY - 50), 62,
                new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(120, 70, 200, 160), new Color(70, 40, 120, 50), new Color(70, 40, 120, 0)}));
        g.fill(new Ellipse2D.Double(cx - 58, baseY - 104, 116, 116));
        g.setComposite(AlphaComposite.SrcOver);

        double lean = pose == 3 ? 0.05 : (pose == 1 ? -0.03 : (pose == 2 ? 0.03 : 0.0));

        // Cuerpo con capucha
        g.translate(cx, baseY - 30);
        g.rotate(lean);
        g.setColor(SHADOW_BODY);
        g.fill(new RoundRectangle2D.Double(-10, -18, 20, 34, 8, 8));
        Path2D hood = new Path2D.Double();
        hood.moveTo(-11, -16);
        hood.curveTo(-13, -26, 13, -26, 11, -16);
        hood.curveTo(11, -10, 6, -8, 0, -12);
        hood.curveTo(-6, -8, -11, -10, -11, -16);
        hood.closePath();
        g.setColor(SHADOW_HOOD);
        g.fill(hood);
        g.rotate(-lean);
        g.translate(-cx, -(baseY - 30));

        // Ojos violetas brillantes
        g.setColor(SHADOW_EYE);
        g.fillOval((int) (cx - 7), (int) (baseY - 50), 5, 6);
        g.fillOval((int) (cx + 2), (int) (baseY - 50), 5, 6);

        // Jirones de humo
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        g.setColor(new Color(110, 80, 170, 160));
        g.fillOval((int) (cx - 18), (int) (baseY - 40 + (pose == 1 ? 4 : 0)), 8, 8);
        g.fillOval((int) (cx + 10), (int) (baseY - 34 - (pose == 2 ? 4 : 0)), 9, 9);
        g.setComposite(AlphaComposite.SrcOver);

        g.dispose();
    }

    // ================================================================
    // Coleccionables
    // ================================================================

    static void drawAlbor(Graphics2D g, int w, int h) {
        double cx = w / 2.0;
        double cy = h / 2.0;

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g.setPaint(new RadialGradientPaint((float) cx, (float) cy, 30,
                new float[]{0f, 1f},
                new Color[]{new Color(255, 250, 220, 230), new Color(255, 250, 220, 0)}));
        g.fill(new Ellipse2D.Double(cx - 30, cy - 30, 60, 60));
        g.setComposite(AlphaComposite.SrcOver);

        // Llama: gota de luz blanca con núcleo dorado
        Path2D flame = new Path2D.Double();
        flame.moveTo(cx, cy - 20);
        flame.curveTo(cx + 12, cy - 4, cx + 10, cy + 12, cx, cy + 14);
        flame.curveTo(cx - 10, cy + 12, cx - 12, cy - 4, cx, cy - 20);
        flame.closePath();
        g.setColor(new Color(255, 252, 235));
        g.fill(flame);

        g.setColor(new Color(255, 216, 144));
        g.fillOval((int) (cx - 3.5), (int) (cy - 6), 7, 8);
    }

    static void drawObsidian(Graphics2D g, int w, int h) {
        double cx = w / 2.0;
        double cy = h / 2.0;

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g.setPaint(new RadialGradientPaint((float) cx, (float) cy, 30,
                new float[]{0f, 1f},
                new Color[]{new Color(150, 100, 230, 180), new Color(150, 100, 230, 0)}));
        g.fill(new Ellipse2D.Double(cx - 30, cy - 30, 60, 60));
        g.setComposite(AlphaComposite.SrcOver);

        // Cristal pentagonal negro con facetas violetas
        Path2D crystal = new Path2D.Double();
        crystal.moveTo(cx, cy - 20);
        crystal.lineTo(cx + 13, cy - 4);
        crystal.lineTo(cx + 8, cy + 16);
        crystal.lineTo(cx - 8, cy + 16);
        crystal.lineTo(cx - 13, cy - 4);
        crystal.closePath();
        g.setColor(new Color(24, 18, 40));
        g.fill(crystal);
        g.setColor(new Color(150, 100, 230));
        g.setStroke(new BasicStroke(2));
        g.draw(crystal);

        g.setColor(new Color(150, 100, 230, 200));
        g.fill(new RoundRectangle2D.Double(cx - 2, cy - 8, 5, 7, 2, 2));
        g.fill(new RoundRectangle2D.Double(cx - 6, cy + 4, 5, 6, 2, 2));
    }

    // ================================================================
    // Peligros
    // ================================================================

    static void drawSpikes(Graphics2D g, int w, int h) {
        int count = Math.max(3, w / 22);
        double step = (double) w / count;
        for (int i = 0; i < count; i++) {
            Path2D spike = new Path2D.Double();
            spike.moveTo(i * step + 3, h);
            spike.lineTo(i * step + step / 2, 4);
            spike.lineTo((i + 1) * step - 3, h);
            spike.closePath();
            g.setColor(new Color(52, 40, 30));
            g.fill(spike);
            g.setColor(new Color(140, 110, 80));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(spike);
        }
        // Base
        g.setColor(new Color(40, 30, 22));
        g.fill(new RoundRectangle2D.Double(0, h - 8, w, 8, 3, 3));
    }

    static void drawPenumbra(Graphics2D g, int w, int h) {
        g.setColor(new Color(30, 24, 48, 210));
        g.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
        // Niebla
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g.setColor(new Color(96, 72, 140, 120));
        for (int i = 0; i < 6; i++) {
            g.fillOval((int) (i * (w / 5.0) - 10), (int) (h / 2.0 - 6 + Math.sin(i * 1.7) * 8), 40, 14);
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(new Color(96, 72, 140, 220));
        g.setStroke(new BasicStroke(2));
        g.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 12, 12));
    }

    static void drawLuminosity(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(255, 236, 180, 210), w, h, new Color(255, 214, 140, 210)));
        g.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g.setColor(new Color(255, 255, 240, 150));
        for (int i = 0; i < 5; i++) {
            g.fillOval((int) (i * (w / 4.0)), (int) (h / 2.0 - 4 + Math.cos(i * 2.1) * 9), 34, 10);
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(new Color(220, 180, 90, 230));
        g.setStroke(new BasicStroke(2));
        g.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 12, 12));
    }

    // ================================================================
    // Plataformas y escaleras
    // ================================================================

    static void drawPlatform(Graphics2D g, int w, int h, boolean moving) {
        Color base = moving ? new Color(74, 56, 38) : new Color(58, 42, 26);
        g.setColor(base);
        g.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));

        // Textura: grietas y piedras
        g.setColor(new Color(40, 28, 18, 160));
        g.fill(new RoundRectangle2D.Double(0, h - 6, w, 6, 4, 4));
        g.setColor(new Color(120, 92, 64, 140));
        g.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < w / 48; i++) {
            int x = 14 + i * 48;
            g.drawLine(x, 4, x, h - 8);
        }
        // Borde superior iluminado
        g.setColor(new Color(200, 170, 120, 120));
        g.setStroke(new BasicStroke(2));
        g.drawLine(2, 2, w - 2, 2);

        if (moving) {
            // Rieles indicadores de dirección
            g.setColor(new Color(216, 184, 120, 200));
            g.fill(new RoundRectangle2D.Double(w / 2.0 - 14, h / 2.0 - 3, 28, 6, 3, 3));
            g.setColor(new Color(231, 200, 130));
            g.fillOval(w - 18, (int) (h / 2.0 - 6), 12, 12);
        }
    }

    static void drawLadder(Graphics2D g, int w, int h) {
        g.setColor(new Color(122, 92, 64, 200));
        g.fill(new RoundRectangle2D.Double(2, 0, 8, h, 4, 4));
        g.fill(new RoundRectangle2D.Double(w - 10, 0, 8, h, 4, 4));
        int rungs = Math.max(4, h / 34);
        g.setColor(new Color(74, 56, 40, 220));
        for (int i = 0; i <= rungs; i++) {
            double y = 6 + i * (h - 12) / (double) rungs;
            g.fill(new RoundRectangle2D.Double(4, y, w - 8, 7, 3, 3));
        }
    }

    // ================================================================
    // Puertas
    // ================================================================

    static void drawDoorLight(Graphics2D g, int w, int h, boolean open) {
        // Marco de piedra clara
        g.setColor(new Color(120, 100, 78));
        g.fill(new RoundRectangle2D.Double(4, 4, w - 8, h - 8, 14, 14));
        g.setColor(new Color(60, 46, 32));
        g.setStroke(new BasicStroke(4));
        g.draw(new RoundRectangle2D.Double(4, 4, w - 8, h - 8, 14, 14));

        // Arco interior
        Path2D arch = new Path2D.Double();
        arch.moveTo(w * 0.2, h - 8);
        arch.lineTo(w * 0.2, h * 0.4);
        arch.curveTo(w * 0.2, h * 0.12, w * 0.8, h * 0.12, w * 0.8, h * 0.4);
        arch.lineTo(w * 0.8, h - 8);
        arch.closePath();
        if (open) {
            g.setPaint(new RadialGradientPaint(w / 2f, h * 0.4f, w * 0.45f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 252, 230), new Color(255, 220, 150, 220)}));
        } else {
            g.setColor(new Color(88, 74, 60));
        }
        g.fill(arch);

        // Runa del portal
        g.setColor(open ? new Color(255, 244, 200) : new Color(70, 58, 46));
        g.fill(new Ellipse2D.Double(w / 2.0 - 10, h * 0.42, 20, 20));
        g.setColor(open ? new Color(255, 214, 140) : new Color(120, 100, 78));
        g.setStroke(new BasicStroke(2));
        g.draw(new Ellipse2D.Double(w / 2.0 - 10, h * 0.42, 20, 20));
        g.drawLine((int) (w / 2.0), (int) (h * 0.42), (int) (w / 2.0), (int) (h * 0.42 + 20));

        if (!open) {
            // Sello de bloqueo
            g.setColor(new Color(40, 32, 24, 210));
            g.fill(new RoundRectangle2D.Double(w / 2.0 - 16, h / 2.0 - 8, 32, 16, 6, 6));
            g.setColor(new Color(216, 184, 120));
            g.setStroke(new BasicStroke(2));
            g.draw(new RoundRectangle2D.Double(w / 2.0 - 16, h / 2.0 - 8, 32, 16, 6, 6));
            g.drawLine((int) (w / 2.0 - 10), (int) (h / 2.0), (int) (w / 2.0 + 10), (int) (h / 2.0));
        }
    }

    static void drawDoorShadow(Graphics2D g, int w, int h, boolean open) {
        g.setColor(new Color(34, 28, 44));
        g.fill(new RoundRectangle2D.Double(4, 4, w - 8, h - 8, 14, 14));
        g.setColor(new Color(14, 10, 20));
        g.setStroke(new BasicStroke(4));
        g.draw(new RoundRectangle2D.Double(4, 4, w - 8, h - 8, 14, 14));

        Path2D arch = new Path2D.Double();
        arch.moveTo(w * 0.2, h - 8);
        arch.lineTo(w * 0.2, h * 0.4);
        arch.curveTo(w * 0.2, h * 0.12, w * 0.8, h * 0.12, w * 0.8, h * 0.4);
        arch.lineTo(w * 0.8, h - 8);
        arch.closePath();
        if (open) {
            g.setPaint(new RadialGradientPaint(w / 2f, h * 0.4f, w * 0.45f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(200, 160, 255), new Color(110, 60, 190, 220)}));
        } else {
            g.setColor(new Color(24, 20, 32));
        }
        g.fill(arch);

        // Runa del portal (triángulo invertido)
        g.setColor(open ? new Color(220, 190, 255) : new Color(52, 44, 66));
        Path2D rune = new Path2D.Double();
        rune.moveTo(w / 2.0, h * 0.60);
        rune.lineTo(w / 2.0 - 12, h * 0.38);
        rune.lineTo(w / 2.0 + 12, h * 0.38);
        rune.closePath();
        g.fill(rune);

        if (!open) {
            g.setColor(new Color(12, 8, 16, 230));
            g.fill(new RoundRectangle2D.Double(w / 2.0 - 16, h / 2.0 - 8, 32, 16, 6, 6));
            g.setColor(new Color(150, 100, 230));
            g.setStroke(new BasicStroke(2));
            g.draw(new RoundRectangle2D.Double(w / 2.0 - 16, h / 2.0 - 8, 32, 16, 6, 6));
            g.drawLine((int) (w / 2.0 - 10), (int) (h / 2.0), (int) (w / 2.0 + 10), (int) (h / 2.0));
        }
    }

    // ================================================================
    // Iconos UI
    // ================================================================

    static void drawLock(Graphics2D g, int w, int h) {
        g.setColor(new Color(216, 184, 120, 220));
        g.setStroke(new BasicStroke(4));
        g.drawArc(w / 2 - 12, h / 2 - 16, 24, 24, 0, 180);
        g.setColor(new Color(58, 42, 26, 235));
        g.fill(new RoundRectangle2D.Double(w / 2 - 18, h / 2 - 4, 36, 26, 6, 6));
        g.setColor(new Color(216, 184, 120, 220));
        g.draw(new RoundRectangle2D.Double(w / 2 - 18, h / 2 - 4, 36, 26, 6, 6));
        g.fillOval(w / 2 - 3, h / 2 + 2, 6, 10);
    }

    static void drawPauseIcon(Graphics2D g, int w, int h) {
        g.setColor(PARCHMENT);
        g.fill(new RoundRectangle2D.Double(w / 2.0 - 16, h / 2.0 - 16, 12, 32, 4, 4));
        g.fill(new RoundRectangle2D.Double(w / 2.0 + 4, h / 2.0 - 16, 12, 32, 4, 4));
    }

    static void drawArrow(Graphics2D g, int w, int h, boolean right) {
        g.setColor(PARCHMENT);
        g.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double cx = w / 2.0;
        double cy = h / 2.0;
        int dir = right ? 1 : -1;
        g.drawLine((int) (cx - dir * 14), (int) (cy + 14), (int) (cx + dir * 12), (int) cy);
        g.drawLine((int) (cx + dir * 12), (int) cy, (int) (cx - dir * 14), (int) (cy - 14));
    }

    static void drawLogoText(Graphics2D g, int w, int h) {
        // Texto del logo (el único texto dentro de una imagen: el logo no es un botón)
        g.setColor(PARCHMENT);
        g.setFont(new Font("Serif", Font.BOLD, 58));
        g.drawString("Luz y Sombra", w / 2 - 190, h / 2 + 20);
        g.setColor(new Color(216, 184, 120, 200));
        g.setFont(new Font("Serif", Font.ITALIC, 18));
        g.drawString("un ritual cooperativo de luz y oscuridad", w / 2 - 175, h / 2 + 48);
    }

    static void drawLockIconForLevel(Graphics2D g, int w, int h, int level, boolean locked, boolean completed) {
        // Número del nivel
        g.setColor(completed ? new Color(150, 210, 150) : (locked ? new Color(120, 110, 100) : PARCHMENT));
        g.setFont(new Font("Serif", Font.BOLD, 60));
        String text = String.valueOf(level);
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (w - tw) / 2, h / 2 + 20);
        if (locked) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
            drawLock(g, w, h);
            g.setComposite(AlphaComposite.SrcOver);
        }
    }
}
