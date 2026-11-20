package com.clearview;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;

/** Pink blossom background with falling petals, used by the menu and the loading screens. */
public final class Blossom {
    private static final int PETALS = 64;
    private static final int[] COLORS = {0xCCFFB7D5, 0xCCFF8FB8, 0xCCFFD1E3, 0xCCF48FB1};

    public static boolean isLoadingScreen(Screen s) {
        if (s instanceof ConnectScreen || s instanceof ProgressScreen || s instanceof MessageScreen
                || s instanceof LevelLoadingScreen || s instanceof ReconfiguringScreen) {
            return true;
        }
        // any other screen with no buttons that can't be closed with Esc is a "please wait" screen
        return !s.shouldCloseOnEsc() && s.children().isEmpty();
    }

    public static void drawBackground(DrawContext ctx, int w, int h) {
        float t = (System.currentTimeMillis() % 3_600_000L) / 1000f;
        ctx.fillGradient(0, 0, w, h, 0xFF2B1428, 0xFF0F0A1E);

        for (int i = 0; i < 3; i++) {
            int by = (int) (h * (0.2f + 0.3f * i) + 18 * Math.sin(t * 0.5f + i * 2));
            int c = i == 1 ? 0x22FF8FB8 : 0x18FFB7D5;
            ctx.fillGradient(0, by - 40, w, by, 0x00FF8FB8, c);
            ctx.fillGradient(0, by, w, by + 40, c, 0x00FF8FB8);
        }

        for (int i = 0; i < PETALS; i++) {
            float speed = 10f + h01(i, 1) * 22f;
            int size = 1 + (int) (h01(i, 2) * 2.99f);
            float y = (t * speed + h01(i, 3) * (h + 40)) % (h + 40) - 20;
            float x = (h01(i, 4) * w + t * (3f + h01(i, 5) * 6f) + 14f * (float) Math.sin(t * 0.8f + i)) % w;
            if (x < 0) x += w;
            drawPetal(ctx, (int) x, (int) y, size, COLORS[i % COLORS.length], i % 2 == 0);
        }
    }

    private static void drawPetal(DrawContext ctx, int x, int y, int s, int c, boolean flip) {
        if (flip) {
            ctx.fill(x + s, y, x + 2 * s, y + s, c);
            ctx.fill(x, y + s, x + 3 * s, y + 2 * s, c);
            ctx.fill(x + s, y + 2 * s, x + 2 * s, y + 3 * s, c);
        } else {
            ctx.fill(x, y, x + 2 * s, y + s, c);
            ctx.fill(x + s, y + s, x + 3 * s, y + 2 * s, c);
            ctx.fill(x, y + 2 * s, x + 2 * s, y + 3 * s, c);
        }
    }

    private static float h01(int i, int k) {
        int x = i * 374761393 + k * 668265263;
        x = (x ^ (x >>> 13)) * 1274126177;
        x ^= x >>> 16;
        return (x & 0xFFFFFF) / (float) 0x1000000;
    }

    private Blossom() {}
}
