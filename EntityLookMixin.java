package com.clearview.mixin;

import com.clearview.FreeLook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* method_5872 = Entity.changeLookDirection(double, double) (Minecraft 1.21.11) */
@Mixin(Entity.class)
public abstract class EntityLookMixin {

    @Inject(method = "method_5872", at = @At("HEAD"), cancellable = true, remap = false)
    private void clearview$freeLookTurn(double dx, double dy, CallbackInfo ci) {
        if (!FreeLook.active) return;
        if ((Object) this != MinecraftClient.getInstance().player) return;
        FreeLook.turn(dx, dy);
        ci.cancel();
    }
}
