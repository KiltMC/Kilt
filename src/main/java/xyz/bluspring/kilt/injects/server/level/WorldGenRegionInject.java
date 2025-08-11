package xyz.bluspring.kilt.injects.server.level;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.entity.MobInjection;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionInject {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void kilt$checkSpawnCancelled(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Mob mob && ((MobInjection) mob).isSpawnCancelled()) {
            cir.setReturnValue(false);
        }
    }
}
