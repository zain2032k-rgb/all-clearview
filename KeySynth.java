package com.clearview;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Plays the recorded keyboard samples (48 kHz, 16-bit, stereo) through a small mixer thread.
 * Recordings: SoberClient by Violet (GPL-3.0-only), see NOTICE.md.
 */
public final class KeySynth {
    private static final int RATE = 48000;
    private static final int CHUNK = 256;
    private static final String BASE = "/assets/clearview/keysamples/";

    private enum Kind {
        PRESS, RELEASE, SPACE, SPACE_RELEASE, STAB, STAB_RELEASE;

        boolean release() {
            return this == RELEASE || this == SPACE_RELEASE || this == STAB_RELEASE;
        }

        String prefix() {
            return name().toLowerCase() + "_";
        }
    }

    private static final Map<String, short[][]> SETS = new HashMap<>();
    private static short[] mouseClick;
    private static final List<Voice> VOICES = new CopyOnWriteArrayList<>();
    private static final Random RND = new Random();

    private static volatile boolean ready;
    private static volatile boolean failed;
    private static boolean started;

    private static final class Voice {
        final short[] data;
        final int frames;
        final float gl;
        final float gr;
        final float step;
        double pos;

        Voice(short[] data, float volume, float pitch) {
            this.data = data;
            this.frames = data.length / 2;
            this.gl = volume;
            this.gr = volume;
            this.step = pitch;
        }
    }

    public static synchronized void init() {
        if (started) return;
        started = true;
        Thread t = new Thread(KeySynth::run, "ClearView-Keys");
        t.setDaemon(true);
        t.start();
    }

    // ---- choosing the sample (same rules as the recorded keyboards were cut for) ----

    private static int mixKey(int key) {
        int h = key * -1640531535;
        h ^= h >>> 15;
        h *= -2048144789;
        h ^= h >>> 13;
        return h;
    }

    private static Kind kind(int key, boolean release) {
        if (key == 32) return release ? Kind.SPACE_RELEASE : Kind.SPACE;
        boolean stab = key == 257 || key == 335 || key == 259 || key == 340 || key == 344 || key == 258 || key == 280;
        if (stab) return release ? Kind.STAB_RELEASE : Kind.STAB;
        return release ? Kind.RELEASE : Kind.PRESS;
    }

    private static short[][] variants(KeyProfile p, Kind slot) {
        short[][] v = SETS.get(p.folder + "/" + slot.prefix());
        return v == null ? new short[0][] : v;
    }

    private static Kind slot(KeyProfile p, Kind kind) {
        if ((kind == Kind.SPACE || kind == Kind.SPACE_RELEASE) && variants(p, kind).length > 0) return kind;
        return kind.release() ? Kind.RELEASE : Kind.PRESS;
    }

    public static void play(KeyProfile profile, int key, boolean release, float volume) {
        if (!started) init();
        if (!ready || failed || volume <= 0f || VOICES.size() > 24) return;

        Kind kind = kind(key, release);
        Kind slot = slot(profile, kind);
        short[][] vars = variants(profile, slot);
        if (vars.length == 0) return;

        boolean realSpace = slot == Kind.SPACE || slot == Kind.SPACE_RELEASE;
        int hash = mixKey(key);
        int variant = realSpace ? RND.nextInt(vars.length) : Math.floorMod(hash, vars.length);
        float pitch = realSpace ? 1f : 1f + (Math.floorMod(hash >>> 8, 1000) / 1000f - 0.5f) * 0.07f;
        if (!realSpace && (kind == Kind.SPACE || kind == Kind.SPACE_RELEASE)) {
            pitch *= 0.9f;
        } else if (kind == Kind.STAB || kind == Kind.STAB_RELEASE) {
            pitch *= 0.95f;
        }
        VOICES.add(new Voice(vars[variant], Math.min(1f, volume), pitch));
    }

    public static void playMouse(float volume) {
        if (!started) init();
        if (!ready || failed || volume <= 0f || mouseClick == null || VOICES.size() > 24) return;
        VOICES.add(new Voice(mouseClick, Math.min(1f, volume), 1f));
    }

    // ---- loading + mixing ----

    private static short[] readPcm(String path) {
        try (InputStream in = KeySynth.class.getResourceAsStream(path)) {
            if (in == null) return null;
            byte[] b = in.readAllBytes();
            short[] s = new short[b.length / 2];
            for (int i = 0; i < s.length; i++) {
                s[i] = (short) ((b[2 * i] & 0xFF) | (b[2 * i + 1] << 8));
            }
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    private static void loadAll() {
        String[] prefixes = {"press_", "release_", "space_", "space_release_"};
        for (KeyProfile p : KeyProfile.values()) {
            for (String prefix : prefixes) {
                java.util.ArrayList<short[]> list = new java.util.ArrayList<>();
                for (int n = 0; n < 64; n++) {
                    short[] s = readPcm(BASE + p.folder + "/" + prefix + n + ".pcm");
                    if (s == null) break;
                    list.add(s);
                }
                SETS.put(p.folder + "/" + prefix, list.toArray(new short[0][]));
            }
        }
        mouseClick = readPcm(BASE + "mouse/click_0.pcm");
    }

    private static void run() {
        try {
            loadAll();
            AudioFormat fmt = new AudioFormat(RATE, 16, 2, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, 1024 * 4);
            line.start();
            ready = true;

            float[] acc = new float[CHUNK * 2];
            byte[] out = new byte[CHUNK * 4];
            while (true) {
                Arrays.fill(acc, 0f);
                for (Voice v : VOICES) {
                    for (int i = 0; i < CHUNK; i++) {
                        int idx = (int) v.pos;
                        if (idx >= v.frames - 1) {
                            v.pos = v.frames;
                            break;
                        }
                        float frac = (float) (v.pos - idx);
                        int a = idx * 2;
                        float l = (v.data[a] + (v.data[a + 2] - v.data[a]) * frac) / 32768f;
                        float r = (v.data[a + 1] + (v.data[a + 3] - v.data[a + 1]) * frac) / 32768f;
                        acc[2 * i] += l * v.gl;
                        acc[2 * i + 1] += r * v.gr;
                        v.pos += v.step;
                    }
                }
                VOICES.removeIf(v -> v.pos >= v.frames - 1);
                for (int i = 0; i < acc.length; i++) {
                    float s = Math.max(-1f, Math.min(1f, acc[i]));
                    int iv = (int) (s * 32767f);
                    out[2 * i] = (byte) iv;
                    out[2 * i + 1] = (byte) (iv >> 8);
                }
                line.write(out, 0, out.length);
            }
        } catch (Throwable t) {
            failed = true;
        }
    }

    private KeySynth() {}
}
