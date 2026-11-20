package com.clearview.mixin;

import com.clearview.Blossom;
import com.clearview.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* method_25420 = Screen.renderBackground(DrawContext, int, int, float) (Minecraft 1.21.11) */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "method_25420", at = @At("HEAD"), cancellable = true, remap = false)
    private void clearview$blossomBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.blossomLoading) return;
        Screen self = (Screen) (Object) this;
        if (!Blossom.isLoadingScreen(self)) return;
        Blossom.drawBackground(context, self.width, self.height);
        ci.cancel();
    }
}
