package com.clearview.gui;

import com.clearview.ClearViewClient;
import com.clearview.Config;
import com.clearview.FreeLook;
import com.clearview.KeySynth;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Panel-style settings window: tabs on the left, settings on the right. */
public class ClearSettingsScreen extends Screen {
    private static final String[] TABS = {"Swing", "Items", "Keys", "View", "Menu", "Loading", "Boost"};
    private static final int SIDEBAR = 84;
    private static int tab = 0;

    private final Screen parent;
    private final long openedAt = System.currentTimeMillis();
    private final List<Row> rows = new ArrayList<>();
    private int wx, wy, ww, wh;
    private int rowX, rowW, nextY;

    private enum Kind { TOGGLE, CYCLE, SLIDER, BIND }

    private static final class Row {
        final Kind kind;
        final String label;
        final int x, y, w, h;
        BooleanSupplier on;
        Supplier<String> text;
        IntSupplier value;
        int min, max;
        String unit = "";

        Row(Kind kind, String label, int x, int y, int w, int h) {
            this.kind = kind;
            this.label = label;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private static final class RowSlider extends SliderWidget {
        private final int min, max, step;
        private final IntConsumer setter;
        private int last;

        RowSlider(int x, int y, int w, int h, int min, int max, int step, int cur, IntConsumer setter) {
            super(x, y, w, h, Text.empty(), (cur - min) / (double) (max - min));
            this.min = min;
            this.max = max;
            this.step = step;
            this.setter = setter;
            this.last = cur;
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.empty());
        }

        @Override
        protected void applyValue() {
            int v = min + (int) Math.round(value * (max - min) / step) * step;
            v = Math.max(min, Math.min(max, v));
            if (v != last) {
                last = v;
                setter.accept(v);
                Config.save();
            }
        }
    }

    public ClearSettingsScreen(Screen parent) {
        super(Text.literal("ClearView"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rows.clear();
        FreeLook.capturing = false;
        ww = Math.min(460, width - 16);
        wh = Math.min(272, height - 16);
        wx = (width - ww) / 2;
        wy = (height - wh) / 2;

        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
                tab = idx;
                clearAndInit();
            }).dimensions(wx + 6, wy + 32 + i * 22, SIDEBAR - 12, 20).build());
        }

        rowX = wx + SIDEBAR + 6;
        rowW = ww - SIDEBAR - 14;
        nextY = wy + 32;

        switch (tab) {
            case 0 -> {
                cycle("Swing style", () -> Config.swingStyle.label, () -> Config.swingStyle = Config.swingStyle.next());
                slider("Swing size", 25, 150, 5, "%", () -> Config.strength, v -> Config.strength = v);
                slider("Swing speed (lower = slower)", 25, 200, 5, "%", () -> Config.swingSpeed, v -> Config.swingSpeed = v);
                toggle("No equip dip", () -> Config.noEquipDip, () -> Config.noEquipDip = !Config.noEquipDip);
            }
            case 1 -> {
                slider("Item size", 30, 100, 5, "%", () -> Config.itemScale, v -> Config.itemScale = v);
                slider("Item X", -30, 30, 1, "", () -> Config.offsetX, v -> Config.offsetX = v);
                slider("Item Y", -30, 30, 1, "", () -> Config.offsetY, v -> Config.offsetY = v);
                slider("Item Z", -30, 30, 1, "", () -> Config.offsetZ, v -> Config.offsetZ = v);
            }
            case 2 -> {
                toggle("Creamy Keys", () -> Config.keysOn, () -> {
                    Config.keysOn = !Config.keysOn;
                    if (Config.keysOn) KeySynth.init();
                });
                cycle("Key sound", () -> Config.keyProfile.label, () -> {
                    Config.keyProfile = Config.keyProfile.next();
                    KeySynth.play(Config.keyProfile, 65, false, Config.keyVolume / 100f);
                });
                slider("Volume", 10, 100, 5, "%", () -> Config.keyVolume, v -> Config.keyVolume = v);
                toggle("Release sound", () -> Config.keyRelease, () -> Config.keyRelease = !Config.keyRelease);
                slider("Cooldown", 0, 300, 10, "ms", () -> Config.keyCooldown, v -> Config.keyCooldown = v);
                toggle("Play in game", () -> Config.keyInGame, () -> Config.keyInGame = !Config.keyInGame);
                toggle("Movement keys too", () -> Config.keyMovement, () -> Config.keyMovement = !Config.keyMovement);
                toggle("Mouse clicks", () -> Config.keyMouse, () -> Config.keyMouse = !Config.keyMouse);
            }
            case 3 -> {
                toggle("Free look", () -> Config.freeLook, () -> Config.freeLook = !Config.freeLook);
                bind("Free look key", () -> FreeLook.capturing ? "Press a key..." : "[ " + FreeLook.bindName() + " ]");
                cycle("Free look mode", () -> Config.freeLookToggle ? "Toggle" : "Hold", () -> Config.freeLookToggle = !Config.freeLookToggle);
                toggle("Third person while looking", () -> Config.freeLookThird, () -> Config.freeLookThird = !Config.freeLookThird);
                slider("Camera distance", 2, 20, 1, "m", () -> Config.freeLookDistance, v -> Config.freeLookDistance = v);
                toggle("Camera through walls", () -> Config.freeLookWalls, () -> Config.freeLookWalls = !Config.freeLookWalls);
                toggle("Clear weather", () -> Config.clearWeather, () -> Config.clearWeather = !Config.clearWeather);
            }
            case 4 -> {
                cycle("Main menu", () -> Config.menuStyle.label, () -> Config.menuStyle = Config.menuStyle.next());
                cycle("Accent color", () -> Config.accent.label, () -> Config.accent = Config.accent.next());
            }
            case 5 -> {
                toggle("Blossom loading screens", () -> Config.blossomLoading, () -> Config.blossomLoading = !Config.blossomLoading);
                toggle("Blossom on Multiplayer", () -> Config.blossomMultiplayer, () -> Config.blossomMultiplayer = !Config.blossomMultiplayer);
                slider("Multiplayer loading time", 300, 2000, 100, "ms", () -> Config.blossomMs, v -> Config.blossomMs = v);
            }
            default -> {
                toggle("FPS boost", () -> Config.fpsBoost, () -> ClearViewClient.setBoost(!Config.fpsBoost));
                toggle("FPS counter", () -> Config.showFps, () -> Config.showFps = !Config.showFps);
            }
        }
    }

    private void toggle(String label, BooleanSupplier on, Runnable flip) {
        Row r = new Row(Kind.TOGGLE, label, rowX, nextY, rowW, 21);
        r.on = on;
        rows.add(r);
        addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
            flip.run();
            Config.save();
        }).dimensions(r.x, r.y, r.w, r.h).build());
        nextY += 25;
    }

    private void cycle(String label, Supplier<String> text, Runnable next) {
        Row r = new Row(Kind.CYCLE, label, rowX, nextY, rowW, 21);
        r.text = text;
        rows.add(r);
        addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
            next.run();
            Config.save();
        }).dimensions(r.x, r.y, r.w, r.h).build());
        nextY += 25;
    }

    private void bind(String label, Supplier<String> text) {
        Row r = new Row(Kind.BIND, label, rowX, nextY, rowW, 21);
        r.text = text;
        rows.add(r);
        addDrawableChild(ButtonWidget.builder(Text.empty(), b -> FreeLook.capturing = true)
                .dimensions(r.x, r.y, r.w, r.h).build());
        nextY += 25;
    }

    private void slider(String label, int min, int max, int step, String unit, IntSupplier get, IntConsumer set) {
        Row r = new Row(Kind.SLIDER, label, rowX, nextY, rowW, 21);
        r.value = get;
        r.min = min;
        r.max = max;
        r.unit = unit;
        rows.add(r);
        addDrawableChild(new RowSlider(r.x + r.w - 150, r.y, 100, r.h, min, max, step, get.getAsInt(), set));
        nextY += 25;
    }

    private static boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Rounded rectangle (1px corners). */
    private static void rr(DrawContext c, int x1, int y1, int x2, int y2, int col) {
        c.fill(x1 + 1, y1, x2 - 1, y1 + 1, col);
        c.fill(x1, y1 + 1, x2, y2 - 1, col);
        c.fill(x1 + 1, y2 - 1, x2 - 1, y2, col);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float fade = Math.min(1f, (System.currentTimeMillis() - openedAt) / 220f);
        ctx.fill(0, 0, width, height, ((int) (0xA0 * fade)) << 24);
        rr(ctx, wx, wy, wx + ww, wy + wh, 0xFF0D1120);
        rr(ctx, wx + 4, wy + 28, wx + SIDEBAR, wy + wh - 4, 0xFF0A0D1A);
        ctx.drawTextWithShadow(textRenderer, "ClearView", wx + 10, wy + 10, Config.accent.color);
        ctx.drawTextWithShadow(textRenderer, "ESC to close", wx + ww - 70, wy + 10, 0xFF6F7592);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int accent = Config.accent.color;
        int soft = (accent & 0x00FFFFFF) | 0x33000000;

        // tabs (painted over the invisible buttons)
        for (int i = 0; i < TABS.length; i++) {
            int x = wx + 6, y = wy + 32 + i * 22, w = SIDEBAR - 12, h = 20;
            boolean sel = i == tab;
            rr(ctx, x, y, x + w, y + h, 0xFF0A0D1A);
            if (sel) {
                rr(ctx, x, y, x + w, y + h, soft);
                ctx.fill(x, y + 3, x + 2, y + h - 3, accent);
            } else if (in(mouseX, mouseY, x, y, w, h)) {
                rr(ctx, x, y, x + w, y + h, 0x22FFFFFF);
            }
            ctx.drawTextWithShadow(textRenderer, TABS[i], x + 10, y + 6, sel ? accent : 0xFFB8BCD6);
        }

        // settings rows
        for (Row r : rows) {
            boolean hov = in(mouseX, mouseY, r.x, r.y, r.w, r.h);
            rr(ctx, r.x, r.y, r.x + r.w, r.y + r.h, hov ? 0xFF1F2440 : 0xFF161A2E);
            int ty = r.y + (r.h - 8) / 2;
            ctx.drawTextWithShadow(textRenderer, r.label, r.x + 8, ty, 0xFFE8EAF6);

            switch (r.kind) {
                case TOGGLE -> {
                    boolean on = r.on.getAsBoolean();
                    int px = r.x + r.w - 32, py = r.y + (r.h - 11) / 2;
                    rr(ctx, px, py, px + 24, py + 11, on ? accent : 0xFF3A3F58);
                    int kx = on ? px + 14 : px + 1;
                    ctx.fill(kx, py + 1, kx + 9, py + 10, 0xFFFFFFFF);
                }
                case BIND -> {
                    String s = r.text.get();
                    int tw = textRenderer.getWidth(s);
                    ctx.drawTextWithShadow(textRenderer, s, r.x + r.w - tw - 8, ty, FreeLook.capturing ? 0xFFFFFFFF : accent);
                }
                case CYCLE -> {
                    String s = "< " + r.text.get() + " >";
                    int tw = textRenderer.getWidth(s);
                    ctx.drawTextWithShadow(textRenderer, s, r.x + r.w - tw - 8, ty, accent);
                }
                case SLIDER -> {
                    int v = r.value.getAsInt();
                    float f = (v - r.min) / (float) (r.max - r.min);
                    int tx = r.x + r.w - 150, tw = 100, ky = r.y + r.h / 2 - 1;
                    ctx.fill(tx, ky, tx + tw, ky + 3, 0xFF3A3F58);
                    ctx.fill(tx, ky, tx + (int) (tw * f), ky + 3, accent);
                    int kx = tx + (int) (tw * f);
                    ctx.fill(kx - 1, ky - 3, kx + 2, ky + 6, 0xFFFFFFFF);
                    String s = v + r.unit;
                    int sw = textRenderer.getWidth(s);
                    ctx.drawTextWithShadow(textRenderer, s, r.x + r.w - sw - 8, ty, 0xFFB8BCD6);
                }
            }
        }
    }

    @Override
    public void removed() {
        FreeLook.capturing = false;
        super.removed();
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
