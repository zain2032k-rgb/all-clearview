package com.clearview.mixin;

import com.clearview.Config;
import com.clearview.SwingMath;
import com.clearview.SwingStyle;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * Runtime (intermediary) names for Minecraft 1.21.11:
 *   method_65816 = swingArm(float swingProgress, MatrixStack, int, Arm)
 *   method_3224  = applyEquipOffset(MatrixStack, Arm, float equipProgress)
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(method = "method_65816", at = @At("HEAD"), cancellable = true, remap = false)
    private void clearview$swing(float swingProgress, MatrixStack matrices, int side, Arm arm, CallbackInfo ci) {
        if (Config.swingStyle == SwingStyle.VANILLA) return;
        SwingMath.apply(Config.swingStyle, swingProgress, matrices, arm == Arm.RIGHT ? 1 : -1, Config.strength / 100f);
        ci.cancel();
    }

    @ModifyVariable(method = "method_3224", at = @At("HEAD"), argsOnly = true, remap = false)
    private float clearview$noDip(float equipProgress) {
        return Config.noEquipDip ? 0f : equipProgress;
    }

    @Inject(method = "method_3224", at = @At("TAIL"), remap = false)
    private void clearview$sizeAndOffset(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
        float side = arm == Arm.RIGHT ? 1f : -1f;
        matrices.translate(side * Config.offsetX / 100f, Config.offsetY / 100f, Config.offsetZ / 100f);
        float s = Config.itemScale / 100f;
        if (s != 1f) matrices.scale(s, s, s);
    }
}
