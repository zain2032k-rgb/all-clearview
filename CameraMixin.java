package com.clearview.mixin;

import com.clearview.Config;
import com.clearview.FreeLook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * Runtime (intermediary) names for Minecraft 1.21.11:
 *   method_19321 = Camera.update(World, Entity, boolean thirdPerson, boolean inverseView, float tickProgress)
 *   method_19325 = setRotation(yaw, pitch)      method_19327 = setPos(x, y, z)
 *   method_19324 = moveBy(x, y, z)              method_19318 = clipToSpace(distance)
 *   field_18721 = cameraY, field_18722 = lastCameraY
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow(remap = false)
    private float field_18721;
    @Shadow(remap = false)
    private float field_18722;

    @Shadow(remap = false)
    protected abstract void method_19325(float yaw, float pitch);

    @Shadow(remap = false)
    protected abstract void method_19327(double x, double y, double z);

    @Shadow(remap = false)
    protected abstract void method_19324(float x, float y, float z);

    @Shadow(remap = false)
    private float method_19318(float distance) {
        throw new AssertionError();
    }

    @Inject(method = "method_19321", at = @At("TAIL"), remap = false)
    private void clearview$freeLook(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView,
                                    float tickProgress, CallbackInfo ci) {
        if (!FreeLook.active) return;
        if (focusedEntity != MinecraftClient.getInstance().player) return;

        this.method_19325(FreeLook.yaw, FreeLook.pitch);
        if (thirdPerson) {
            Vec3d p = focusedEntity.getLerpedPos(tickProgress);
            this.method_19327(p.x, p.y + MathHelper.lerp(tickProgress, this.field_18722, this.field_18721), p.z);
            if (inverseView) this.method_19325(FreeLook.yaw + 180.0f, -FreeLook.pitch);
            float distance = Config.freeLookDistance;
            this.method_19324(-(Config.freeLookWalls ? distance : this.method_19318(distance)), 0.0f, 0.0f);
        }
    }
}
