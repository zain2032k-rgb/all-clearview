package com.clearview.gui;

import com.clearview.Blossom;
import com.clearview.Config;
import com.clearview.MenuStyle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ClearTitleScreen extends Screen {

    public ClearTitleScreen() {
        super(Text.literal("ClearView"));
    }

    private boolean minimal() {
        return Config.menuStyle == MenuStyle.MINIMAL;
    }

    @Override
    protected void init() {
        int w = 200;
        int x = minimal() ? 40 : width / 2 - w / 2;
        int y = minimal() ? height / 2 - 40 : height / 2 - 30;

        add("Singleplayer", x, y, w, () -> client.setScreen(new SelectWorldScreen(this)));
        add("Multiplayer", x, y + 24, w, () -> {
            MultiplayerScreen mp = new MultiplayerScreen(this);
            client.setScreen(Config.blossomMultiplayer
                    ? new BlossomLoadingScreen(mp, "Loading servers", Config.blossomMs)
                    : mp);
        });
        add("Options", x, y + 48, 98, () -> client.setScreen(new OptionsScreen(this, client.options)));
        add("ClearView", x + 102, y + 48, 98, () -> client.setScreen(new ClearSettingsScreen(this)));
        add("Quit Game", x, y + 76, w, () -> client.scheduleStop());
    }

    private void add(String label, int x, int y, int w, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> action.run())
                .dimensions(x, y, w, 20).build());
    }

    private static int rgb(float r, float g, float b) {
        int ri = Math.max(0, Math.min(255, (int) r));
        int gi = Math.max(0, Math.min(255, (int) g));
        int bi = Math.max(0, Math.min(255, (int) b));
        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        float t = (System.currentTimeMillis() % 600000L) / 1000f;
        switch (Config.menuStyle) {
            case BLOSSOM -> Blossom.drawBackground(context, width, height);
            case MINIMAL -> context.fill(0, 0, width, height, 0xFF0B0B10);
            case CLASSIC -> context.fillGradient(0, 0, width, height, 0xFF0B1F3A, 0xFF02060F);
            default -> {
                int top = rgb(18 + 14 * (float) Math.sin(t * 0.5), 10 + 18 * (float) Math.sin(t * 0.3 + 2), 50 + 40 * (float) Math.sin(t * 0.4 + 4));
                int bottom = rgb(5 + 8 * (float) Math.sin(t * 0.35 + 1), 25 + 25 * (float) Math.sin(t * 0.45 + 3), 40 + 25 * (float) Math.sin(t * 0.25));
                context.fillGradient(0, 0, width, height, top, bottom);
                for (int i = 0; i < 3; i++) {
                    int by = (int) (height * (0.25 + 0.25 * i) + 20 * Math.sin(t * (0.6 + 0.2 * i) + i * 2));
                    int c = i == 0 ? 0x2200FFAA : i == 1 ? 0x228855FF : 0x2255AAFF;
                    context.fillGradient(0, by - 30, width, by, 0x00000000, c);
                    context.fillGradient(0, by, width, by + 30, c, 0x00000000);
                }
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (minimal()) {
            context.drawTextWithShadow(textRenderer, Text.literal("ClearView"), 40, height / 2 - 62, 0xFFFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("C L E A R V I E W"), width / 2, height / 2 - 62, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("fps + visuals"), width / 2, height / 2 - 50, Config.accent.color);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
