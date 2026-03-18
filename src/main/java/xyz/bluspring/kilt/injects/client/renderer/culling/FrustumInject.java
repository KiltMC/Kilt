// TRACKED HASH: 6f1cdd4a7dd57568eb7b70d3ed1a81d3d3ee248f
package xyz.bluspring.kilt.injects.client.renderer.culling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

@Mixin(Frustum.class)
public class FrustumInject {
    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void kilt$infiniteBoundEarlyExit(AABB aabb, CallbackInfoReturnable<Boolean> cir) {
        if (aabb.isInfinite())
            cir.setReturnValue(true);
    }
}