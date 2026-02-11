package xyz.bluspring.kilt.compat.forge.mixin.littletiles;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.math.vec.LittleHitResult;

// Kilt: Copy pasting the LittleTiles mixin because otherwise it's just broken.
// Kilt TODO: I feel like this can be made better without copying code.
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract Vec3 getEyePosition(float f);

    @Shadow
    public abstract Vec3 getViewVector(float f);

    @Inject(
        method = "pick",
        at = @At("RETURN"),
        cancellable = true,
        require = 1
    )
    public void kilt$littletiles$pick(double reach, float partialTicks, boolean fluid, CallbackInfoReturnable<HitResult> info) {
        Vec3 pos = this.getEyePosition(partialTicks);
        Vec3 view = this.getViewVector(partialTicks);
        Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);

        Entity entity = (Entity) (Object) this;
        HitResult result = info.getReturnValue();
        double var10000;
        if (result != null) {
            var10000 = pos.distanceTo(result.getLocation());
        } else if (entity instanceof Player p) {
            double attrib = 5.0F;
            var10000 = p.isCreative() ? attrib : attrib - 0.5F;
        } else {
            var10000 = 4.0F;
        }

        double reachDistance = var10000;
        LittleHitResult hit = (LittleTiles.ANIMATION_HANDLERS.get(entity.level())).getHit(pos, look, reachDistance);
        if (hit != null) {
            info.setReturnValue(hit);
        }

    }
}
