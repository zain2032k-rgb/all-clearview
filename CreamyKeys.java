package com.clearview;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;

import java.util.HashSet;
import java.util.Set;

/** Plays a keyboard / mouse click sound by chaining onto the game's own GLFW callbacks. */
public final class CreamyKeys {
    private static final Set<Integer> DOWN = new HashSet<>();
    private static final Set<Integer> BUTTONS = new HashSet<>();
    private static long lastSound;
    private static long lastMouse;
    private static boolean installed;

    public static void install(MinecraftClient client) {
        if (installed) return;
        installed = true;
        long handle = client.getWindow().getHandle();

        GLFWKeyCallback[] prevKey = new GLFWKeyCallback[1];
        GLFWKeyCallbackI keyCb = (window, key, scancode, action, mods) -> {
            boolean consumed = false;
            try {
                consumed = onKey(key, action);
            } catch (Throwable ignored) {
            }
            if (!consumed && prevKey[0] != null) prevKey[0].invoke(window, key, scancode, action, mods);
        };
        prevKey[0] = GLFW.glfwSetKeyCallback(handle, keyCb);

        GLFWMouseButtonCallback[] prevMouse = new GLFWMouseButtonCallback[1];
        GLFWMouseButtonCallbackI mouseCb = (window, button, action, mods) -> {
            boolean consumed = false;
            try {
                consumed = onMouse(button, action);
            } catch (Throwable ignored) {
            }
            if (!consumed && prevMouse[0] != null) prevMouse[0].invoke(window, button, action, mods);
        };
        prevMouse[0] = GLFW.glfwSetMouseButtonCallback(handle, mouseCb);

        if (Config.keysOn) KeySynth.init();
    }

    private static boolean isMovement(int key) {
        return key == GLFW.GLFW_KEY_W || key == GLFW.GLFW_KEY_A || key == GLFW.GLFW_KEY_S
                || key == GLFW.GLFW_KEY_D || key == GLFW.GLFW_KEY_SPACE
                || key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_LEFT_CONTROL;
    }

    /** Returns true when the event was used to pick the free look key (the game should not see it). */
    private static boolean onKey(int key, int action) {
        if (FreeLook.capturing && action == GLFW.GLFW_PRESS) {
            FreeLook.captureKey(key);
            return true;
        }
        if (!Config.keysOn || key == -1 || action == GLFW.GLFW_REPEAT) return false;
        if (action == GLFW.GLFW_RELEASE) {
            if (DOWN.remove(key) && Config.keyRelease) play(key, true);
            return false;
        }
        if (!DOWN.add(key)) return false;
        boolean playing = MinecraftClient.getInstance().currentScreen == null;
        if (playing && (!Config.keyInGame || (!Config.keyMovement && isMovement(key)))) return false;
        play(key, false);
        return false;
    }

    private static void play(int key, boolean up) {
        long now = System.currentTimeMillis();
        if (now - lastSound < Config.keyCooldown) return;
        lastSound = now;
        KeySynth.play(Config.keyProfile, key, up, Config.keyVolume / 100f);
    }

    private static boolean onMouse(int button, int action) {
        if (FreeLook.capturing && action == GLFW.GLFW_PRESS) {
            FreeLook.captureMouse(button);
            return true;
        }
        if (!Config.keysOn) return false;
        if (action == GLFW.GLFW_RELEASE) {
            BUTTONS.remove(button);
            return false;
        }
        if (action != GLFW.GLFW_PRESS || !BUTTONS.add(button)) return false;
        boolean noScreen = MinecraftClient.getInstance().currentScreen == null;
        if (!Config.keyMouse || (noScreen && !Config.keyInGame)) return false;
        long now = System.currentTimeMillis();
        if (now - lastMouse < 20L) return false;
        lastMouse = now;
        KeySynth.playMouse(Config.keyVolume / 100f);
        return false;
    }

    private CreamyKeys() {}
}
