package com.clearview.mixin;

import com.clearview.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* method_6028 = LivingEntity.getHandSwingDuration() (Minecraft 1.21.11). Only your own swing is changed, and only visually. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "method_6028", at = @At("RETURN"), cancellable = true, remap = false)
    private void clearview$swingSpeed(CallbackInfoReturnable<Integer> cir) {
        if (Config.swingSpeed == 100) return;
        if ((Object) this != MinecraftClient.getInstance().player) return;
        int base = cir.getReturnValue();
        cir.setReturnValue(Math.max(1, Math.round(base * 100f / Math.max(10, Config.swingSpeed))));
    }
}
