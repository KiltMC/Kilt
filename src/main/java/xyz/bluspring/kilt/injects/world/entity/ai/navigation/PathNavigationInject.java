package xyz.bluspring.kilt.injects.world.entity.ai.navigation;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PathNavigation.class)
public abstract class PathNavigationInject {
    @Shadow @Final protected Mob mob;

    @ModifyExpressionValue(method = "followThePath", at = @At(value = "CONSTANT", args = "doubleValue=0.5"))
    private double kilt$fixMC94054(double original) {
        if (original != 0.5) // mod compat
            return original;

        return (this.mob.getBbWidth() + 1.0) / 2.0;
    }

    // TODO: we need to impl a < to <= but trying to figure out how to properly do this....
}
