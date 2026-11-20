package com.clearview.mixin;

import com.clearview.Config;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * method_8430 = World.getRainGradient(float), method_8478 = World.getThunderGradient(float) (Minecraft 1.21.11).
 * Only the client's own world is changed, so the server keeps its real weather.
 */
@Mixin(World.class)
public abstract class WeatherMixin {

    @Inject(method = "method_8430", at = @At("HEAD"), cancellable = true, remap = false)
    private void clearview$noRain(float delta, CallbackInfoReturnable<Float> cir) {
        if (Config.clearWeather && (Object) this instanceof ClientWorld) cir.setReturnValue(0.0f);
    }

    @Inject(method = "method_8478", at = @At("HEAD"), cancellable = true, remap = false)
    private void clearview$noThunder(float delta, CallbackInfoReturnable<Float> cir) {
        if (Config.clearWeather && (Object) this instanceof ClientWorld) cir.setReturnValue(0.0f);
    }
}
