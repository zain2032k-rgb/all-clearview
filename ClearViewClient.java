package com.clearview;

import com.clearview.gui.ClearSettingsScreen;
import com.clearview.gui.ClearTitleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClearViewClient implements ClientModInitializer {

    private static final String[][] TOTEMS = {
            {"gold", "Gold"}, {"ruby", "Ruby"}, {"sapphire", "Sapphire"}, {"emerald", "Emerald"},
            {"amethyst", "Amethyst"}, {"ice", "Ice"}, {"shadow", "Shadow"}, {"sakura", "Sakura"},
            {"lime", "Lime"}, {"ember", "Ember"}, {"ghost", "Ghost"}, {"copper", "Copper"}
    };

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("clearview", "main"));

    private static KeyBinding openSettings;
    private static KeyBinding togglePerf;
    private static KeyBinding toggleHud;
    private static Snapshot original;

    private record Snapshot(int view, int sim, double entityScale, int biomeBlend,
                            CloudRenderMode clouds, boolean shadows, int maxFps, boolean vsync) {}

    @Override
    public void onInitializeClient() {
        Config.load();

        // Totem skins show up as optional packs: Options > Resource Packs
        ModContainer container = FabricLoader.getInstance().getModContainer("clearview").orElseThrow();
        for (String[] t : TOTEMS) {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    Identifier.of("clearview", "totem_" + t[0]), container,
                    Text.literal("ClearView Totem: " + t[1]), ResourcePackActivationType.NORMAL);
        }

        openSettings = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clearview.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));
        togglePerf = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clearview.toggle_perf", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F8, CATEGORY));
        toggleHud = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clearview.toggle_hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F9, CATEGORY));

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (Config.fpsBoost) applyBoost(client);
            CreamyKeys.install(client);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FreeLook.tick(client);
            while (openSettings.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ClearSettingsScreen(null));
            }
            while (togglePerf.wasPressed()) {
                setBoost(!Config.fpsBoost);
                message(client, "FPS Boost: " + (Config.fpsBoost ? "ON" : "OFF"));
            }
            while (toggleHud.wasPressed()) {
                Config.showFps = !Config.showFps;
                Config.save();
            }
            if (Config.menuStyle != MenuStyle.OFF && client.currentScreen instanceof TitleScreen) {
                client.setScreen(new ClearTitleScreen());
            }
        });

        HudElementRegistry.addLast(Identifier.of("clearview", "fps"), (context, tickCounter) -> {
            if (!Config.showFps) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.hudHidden) return;
            String text = client.getCurrentFps() + " FPS" + (Config.fpsBoost ? "  [Boost]" : "");
            context.drawTextWithShadow(client.textRenderer, text, 4, 4, 0xFFFFFFFF);
        });
    }

    public static void setBoost(boolean on) {
        Config.fpsBoost = on;
        MinecraftClient client = MinecraftClient.getInstance();
        if (on) applyBoost(client); else restore(client);
        Config.save();
    }

    private static void applyBoost(MinecraftClient client) {
        GameOptions o = client.options;
        if (original == null) {
            original = new Snapshot(
                    o.getViewDistance().getValue(), o.getSimulationDistance().getValue(),
                    o.getEntityDistanceScaling().getValue(), o.getBiomeBlendRadius().getValue(),
                    o.getCloudRenderMode().getValue(), o.getEntityShadows().getValue(),
                    o.getMaxFps().getValue(), o.getEnableVsync().getValue());
        }
        o.getViewDistance().setValue(Math.min(original.view(), 8));
        o.getSimulationDistance().setValue(Math.min(original.sim(), 6));
        o.getEntityDistanceScaling().setValue(0.75);
        o.getBiomeBlendRadius().setValue(1);
        o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        o.getEntityShadows().setValue(false);
        o.getMaxFps().setValue(260);
        o.getEnableVsync().setValue(false);
        o.write();
    }

    private static void restore(MinecraftClient client) {
        if (original == null) return;
        GameOptions o = client.options;
        o.getViewDistance().setValue(original.view());
        o.getSimulationDistance().setValue(original.sim());
        o.getEntityDistanceScaling().setValue(original.entityScale());
        o.getBiomeBlendRadius().setValue(original.biomeBlend());
        o.getCloudRenderMode().setValue(original.clouds());
        o.getEntityShadows().setValue(original.shadows());
        o.getMaxFps().setValue(original.maxFps());
        o.getEnableVsync().setValue(original.vsync());
        o.write();
        original = null;
    }

    private static void message(MinecraftClient client, String msg) {
        if (client.player != null) client.player.sendMessage(Text.literal("[ClearView] " + msg), true);
    }
}
