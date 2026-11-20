package com.clearview.gui;

import com.clearview.Blossom;
import com.clearview.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Short blossom loading screen shown before another screen (used when you press Multiplayer). */
public class BlossomLoadingScreen extends Screen {
    private final Screen next;
    private final String label;
    private final int ms;
    private final long start = System.currentTimeMillis();

    public BlossomLoadingScreen(Screen next, String label, int ms) {
        super(Text.literal("Loading"));
        this.next = next;
        this.label = label;
        this.ms = Math.max(100, ms);
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() - start >= ms) client.setScreen(next);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        Blossom.drawBackground(context, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        float p = Math.min(1f, (System.currentTimeMillis() - start) / (float) ms);
        float eased = 1f - (1f - p) * (1f - p);

        int dots = (int) ((System.currentTimeMillis() / 350) % 4);
        String text = label + ".".repeat(dots);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(text), width / 2, height / 2 - 14, 0xFFFFFFFF);

        int bw = 140, bx = width / 2 - bw / 2, by = height / 2 + 2;
        context.fill(bx, by, bx + bw, by + 4, 0x55FFFFFF);
        context.fill(bx, by, bx + (int) (bw * eased), by + 4, Config.accent.color);
    }

    @Override
    public void close() {
        client.setScreen(next);
    }
}
