package xyz.bluspring.kilt.forgeinjects.world.entity.projectile;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WitherSkull.class)
public abstract class WitherSkullInject {
    @ModifyExpressionValue(method = "getBlockExplosionResistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;canDestroy(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean kilt$checkCanEntityDestroy(boolean original, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos) {
        return original || state.canEntityDestroy(level, pos, (WitherSkull) (Object) this);
    }
}
