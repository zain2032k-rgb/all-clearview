package com.clearview;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;

/** Look around your player without turning. State + key handling; the camera itself is moved by CameraMixin. */
public final class FreeLook {
    public static boolean active;
    public static boolean capturing;   // waiting for the next key/mouse button to bind
    public static float yaw;
    public static float pitch;

    private static boolean wasDown;
    private static boolean toggled;
    private static boolean changedPerspective;
    private static Perspective previous;

    public static void tick(MinecraftClient client) {
        boolean usable = Config.freeLook && client.player != null && client.world != null && client.currentScreen == null;
        boolean down = usable && isBindDown(client);
        boolean want;
        if (Config.freeLookToggle) {
            if (down && !wasDown) toggled = !toggled;
            if (!usable) toggled = false;
            want = toggled;
        } else {
            toggled = false;
            want = down;
        }
        wasDown = down;

        if (want && !active) start(client);
        else if (!want && active) stop(client);
    }

    private static boolean isBindDown(MinecraftClient client) {
        int k = Config.freeLookKey;
        if (k < 0) return false;
        long handle = client.getWindow().getHandle();
        return Config.freeLookMouse
                ? GLFW.glfwGetMouseButton(handle, k) == GLFW.GLFW_PRESS
                : GLFW.glfwGetKey(handle, k) == GLFW.GLFW_PRESS;
    }

    private static void start(MinecraftClient client) {
        yaw = client.player.getYaw();
        pitch = client.player.getPitch();
        previous = client.options.getPerspective();
        changedPerspective = false;
        if (Config.freeLookThird && previous.isFirstPerson()) {
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            changedPerspective = true;
        }
        active = true;
    }

    private static void stop(MinecraftClient client) {
        active = false;
        if (changedPerspective && previous != null) {
            client.options.setPerspective(previous);
        }
        changedPerspective = false;
    }

    /** Called from the mouse: same sensitivity scaling the game uses for turning the player. */
    public static void turn(double dx, double dy) {
        yaw += (float) dx * 0.15f;
        pitch = Math.max(-90f, Math.min(90f, pitch + (float) dy * 0.15f));
    }

    // ---- choosing the key ----

    public static void captureKey(int key) {
        capturing = false;
        if (key == GLFW.GLFW_KEY_ESCAPE) return;
        Config.freeLookKey = key;
        Config.freeLookMouse = false;
        Config.save();
    }

    public static void captureMouse(int button) {
        capturing = false;
        if (button < 2) return;   // left / right click just cancel
        Config.freeLookKey = button;
        Config.freeLookMouse = true;
        Config.save();
    }

    public static String bindName() {
        int k = Config.freeLookKey;
        if (k < 0) return "None";
        if (Config.freeLookMouse) return k == 2 ? "Middle Mouse" : "Mouse " + (k + 1);
        if (k >= GLFW.GLFW_KEY_F1 && k <= GLFW.GLFW_KEY_F25) return "F" + (k - GLFW.GLFW_KEY_F1 + 1);
        switch (k) {
            case GLFW.GLFW_KEY_LEFT_ALT: return "Left Alt";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "Right Alt";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "Left Ctrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "Right Ctrl";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "Left Shift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "Right Shift";
            case GLFW.GLFW_KEY_LEFT_SUPER: return "Left Super";
            case GLFW.GLFW_KEY_RIGHT_SUPER: return "Right Super";
            case GLFW.GLFW_KEY_TAB: return "Tab";
            case GLFW.GLFW_KEY_CAPS_LOCK: return "Caps Lock";
            case GLFW.GLFW_KEY_SPACE: return "Space";
            case GLFW.GLFW_KEY_ENTER: return "Enter";
            case GLFW.GLFW_KEY_BACKSPACE: return "Backspace";
            case GLFW.GLFW_KEY_INSERT: return "Insert";
            case GLFW.GLFW_KEY_DELETE: return "Delete";
            case GLFW.GLFW_KEY_HOME: return "Home";
            case GLFW.GLFW_KEY_END: return "End";
            case GLFW.GLFW_KEY_PAGE_UP: return "Page Up";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "Page Down";
            case GLFW.GLFW_KEY_UP: return "Up";
            case GLFW.GLFW_KEY_DOWN: return "Down";
            case GLFW.GLFW_KEY_LEFT: return "Left";
            case GLFW.GLFW_KEY_RIGHT: return "Right";
            default:
                String n = GLFW.glfwGetKeyName(k, 0);
                return n != null ? n.toUpperCase() : "Key " + k;
        }
    }

    private FreeLook() {}
}
