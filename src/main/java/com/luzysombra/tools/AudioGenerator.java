package com.luzysombra.tools;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Genera archivos WAV originales mediante síntesis procedural (sin muestras
 * externas ni derechos de autor): efectos de sonido y una música ambiental
 * de pad épico-místico en bucle.
 * <p>
 * Formato: PCM 16 bits, mono, 44100 Hz. Se ejecuta con Maven:
 * {@code mvn compile exec:java -Dexec.mainClass=com.luzysombra.tools.AudioGenerator}
 */
public final class AudioGenerator {

    private static final int SAMPLE_RATE = 44100;

    private AudioGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path base = Path.of("src/main/resources/assets");
        Files.createDirectories(base.resolve("sounds"));
        Files.createDirectories(base.resolve("music"));

        writeWav(base.resolve("sounds/click.wav"), click());
        writeWav(base.resolve("sounds/jump.wav"), jump());
        writeWav(base.resolve("sounds/collect-light.wav"), chime(880, 1320, 1760, 0.35));
        writeWav(base.resolve("sounds/collect-shadow.wav"), chime(520, 780, 1040, 0.35));
        writeWav(base.resolve("sounds/hurt.wav"), hurt());
        writeWav(base.resolve("sounds/door.wav"), door());
        writeWav(base.resolve("sounds/victory.wav"), victory());
        writeWav(base.resolve("sounds/gameover.wav"), gameOver());
        writeWav(base.resolve("sounds/checkpoint.wav"), chime(660, 880, 1100, 0.3));

        writeWav(base.resolve("music/ambient.wav"), ambientPad(8.0));

        System.out.println("Audio generado correctamente.");
    }

    // ================================================================
    // Síntesis de efectos
    // ================================================================

    /** Clic suave de interfaz. */
    private static double[] click() {
        return envelope(tone(1200, 0.08), 0.002, 0.07);
    }

    /** Salto: barrido ascendente corto. */
    private static double[] jump() {
        int n = (int) (SAMPLE_RATE * 0.18);
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            double t = (double) i / SAMPLE_RATE;
            double freq = 320 + 420 * (t / 0.18);
            out[i] = Math.sin(2 * Math.PI * freq * t) * 0.4;
        }
        return envelope(out, 0.01, 0.16);
    }

    /** Trino en tres notas (coleccionables). */
    private static double[] chime(double f1, double f2, double f3, double dur) {
        int n = (int) (SAMPLE_RATE * dur);
        double[] out = new double[n];
        double[] freqs = {f1, f2, f3};
        double seg = dur / 3.0;
        for (int i = 0; i < n; i++) {
            double t = (double) i / SAMPLE_RATE;
            int idx = Math.min(2, (int) (t / seg));
            double local = t - idx * seg;
            double decay = Math.exp(-local * 8);
            out[i] += Math.sin(2 * Math.PI * freqs[idx] * t) * 0.35 * decay;
            out[i] += Math.sin(2 * Math.PI * freqs[idx] * 2 * t) * 0.1 * decay;
        }
        return envelope(out, 0.005, dur - 0.02);
    }

    /** Daño: disonancia descendente con ruido. */
    private static double[] hurt() {
        int n = (int) (SAMPLE_RATE * 0.35);
        double[] out = new double[n];
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < n; i++) {
            double t = (double) i / SAMPLE_RATE;
            double freq = 300 - 180 * (t / 0.35);
            double tone = Math.sin(2 * Math.PI * freq * t);
            double noise = (rnd.nextDouble() * 2 - 1) * 0.3;
            double wob = Math.sin(2 * Math.PI * 9 * t) * 0.15;
            out[i] = (tone * 0.5 + noise) * (1 + wob);
        }
        return envelope(out, 0.005, 0.34);
    }

    /** Puerta abierta: glissando místico ascendente. */
    private static double[] door() {
        int n = (int) (SAMPLE_RATE * 0.8);
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            double t = (double) i / SAMPLE_RATE;
            double freq = 392 + 330 * (t / 0.8);
            double shimmer = Math.sin(2 * Math.PI * freq * t) * 0.35;
            shimmer += Math.sin(2 * Math.PI * freq * 1.5 * t) * 0.12;
            shimmer *= Math.exp(-t * 2.2);
            out[i] = shimmer;
        }
        return envelope(out, 0.02, 0.78);
    }

    /** Victoria: arpegio ascendente brillante. */
    private static double[] victory() {
        double[] notes = {523.25, 659.25, 783.99, 1046.50, 783.99, 1046.50, 1318.51};
        double dur = 1.4;
        int n = (int) (SAMPLE_RATE * dur);
        double[] out = new double[n];
        double seg = dur / notes.length;
        for (int i = 0; i < n; i++) {
            double t = (double) i / SAMPLE_RATE;
            int idx = Math.min(notes.length - 1, (int) (t / seg));
            double local = t - idx * seg;
            double decay = Math.exp(-local * 6);
            double f = notes[idx];
            out[i] = (Math.sin(2 * Math.PI * f * t) + 0.4 * Math.sin(2 * Math.PI * f * 2 * t)) * 0.3 * decay;
        }
        return envelope(out, 0.005, dur - 0.02);
    }

    /** Derrota: dos notas graves descendentes. */
    private static double[] gameOver() {
        double dur = 1.2;
        int n = (int) (SAMPLE_RATE * dur);
        double[] out = new double[n];
        double[] notes = {196.0, 146.83};
        double seg = dur / 2;
        for (int i = 0; i < n; i++) {
            double t = (double) i / SAMPLE_RATE;
            int idx = Math.min(1, (int) (t / seg));
            double local = t - idx * seg;
            double decay = Math.exp(-local * 3);
            double f = notes[idx];
            out[i] = Math.sin(2 * Math.PI * f * t) * 0.4 * decay;
        }
        return envelope(out, 0.02, dur - 0.02);
    }

    // ================================================================
    // Música ambiental
    // ================================================================

    /** Pad épico-místico: acordes Am - F - C - G con vibrato suave, en bucle limpio. */
    private static double[] ambientPad(double seconds) {
        int n = (int) (SAMPLE_RATE * seconds);
        double[] out = new double[n];
        double loop = seconds;

        // Progresión: Am (220, 261.63, 329.63), F (174.61, 220, 261.63),
        //            C (196, 246.94, 329.63), G (196, 246.94, 293.66)
        double[][] chords = {
                {220.00, 261.63, 329.63, 440.00},
                {174.61, 220.00, 261.63, 349.23},
                {196.00, 246.94, 329.63, 392.00},
                {196.00, 246.94, 293.66, 392.00}
        };
        double chordDur = loop / chords.length;

        for (int i = 0; i < n; i++) {
            double t = (double) i / SAMPLE_RATE;
            int chordIdx = (int) (t / chordDur) % chords.length;
            double local = t - chordIdx * chordDur;

            // Envolvente tipo pad: ataque lento + liberación suave al final del compás
            double attack = Math.min(1, local / 0.6);
            double release = Math.min(1, (chordDur - local) / 0.8);
            double env = Math.max(0, Math.min(attack, release));

            double sample = 0;
            for (double f : chords[chordIdx]) {
                double detune = 1.0 + 0.0015 * Math.sin(2 * Math.PI * 0.13 * t);
                sample += Math.sin(2 * Math.PI * f * detune * t);
            }
            // Shimmer: nota aguda con vibrato
            sample += Math.sin(2 * Math.PI * 880 * t) * 0.12
                    * (0.5 + 0.5 * Math.sin(2 * Math.PI * 0.3 * t));

            out[i] = sample / 5.0 * env * 0.42;
        }

        // Fundido de entrada/salida para un loop sin clic
        int fade = SAMPLE_RATE / 2;
        for (int i = 0; i < fade && i < n; i++) {
            double f = (double) i / fade;
            out[i] *= f * f;
            out[n - 1 - i] *= f * f;
        }
        return out;
    }

    // ================================================================
    // Utilidades de síntesis
    // ================================================================

    private static double[] tone(double freq, double dur) {
        int n = (int) (SAMPLE_RATE * dur);
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            double t = (double) i / SAMPLE_RATE;
            out[i] = Math.sin(2 * Math.PI * freq * t) * 0.4;
        }
        return out;
    }

    /** Aplica envolvente de ataque/liberación a una señal. */
    private static double[] envelope(double[] in, double attack, double release) {
        double[] out = in.clone();
        int a = (int) (SAMPLE_RATE * attack);
        int r = (int) (SAMPLE_RATE * release);
        for (int i = 0; i < out.length; i++) {
            double env = 1;
            if (i < a) {
                env = (double) i / a;
            } else if (i > out.length - r) {
                env = Math.max(0, (double) (out.length - i) / r);
            }
            out[i] *= env;
        }
        return out;
    }

    private static void writeWav(Path path, double[] samples) throws IOException {
        short[] pcm = new short[samples.length];
        for (int i = 0; i < samples.length; i++) {
            double v = Math.max(-1.0, Math.min(1.0, samples[i]));
            pcm[i] = (short) (v * 32767);
        }

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path.toFile()))) {
            int dataSize = pcm.length * 2;
            out.writeBytes("RIFF");
            writeIntLE(out, 36 + dataSize);
            out.writeBytes("WAVE");
            out.writeBytes("fmt ");
            writeIntLE(out, 16);          // tamaño del chunk fmt
            writeShortLE(out, 1);         // PCM
            writeShortLE(out, 1);         // mono
            writeIntLE(out, SAMPLE_RATE);
            writeIntLE(out, SAMPLE_RATE * 2); // byte rate
            writeShortLE(out, 2);         // block align
            writeShortLE(out, 16);        // bits por muestra
            out.writeBytes("data");
            writeIntLE(out, dataSize);
            for (short s : pcm) {
                writeShortLE(out, s);
            }
        }
    }

    private static void writeIntLE(DataOutputStream out, int value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >> 8) & 0xFF);
        out.writeByte((value >> 16) & 0xFF);
        out.writeByte((value >> 24) & 0xFF);
    }

    private static void writeShortLE(DataOutputStream out, int value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >> 8) & 0xFF);
    }
}
