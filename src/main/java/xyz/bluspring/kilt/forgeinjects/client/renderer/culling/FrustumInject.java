// TRACKED HASH: 6f1cdd4a7dd57568eb7b70d3ed1a81d3d3ee248f
package xyz.bluspring.kilt.forgeinjects.client.renderer.culling;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frustum.class)
public class FrustumInject {
    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void kilt$infiniteBoundEarlyExit(AABB aabb, CallbackInfoReturnable<Boolean> cir) {
        if (aabb.equals(IForgeBlockEntity.INFINITE_EXTENT_AABB))
            cir.setReturnValue(true);
    }
}