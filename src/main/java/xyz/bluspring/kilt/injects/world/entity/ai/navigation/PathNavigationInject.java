package xyz.bluspring.kilt.injects.world.entity.ai.navigation;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

@Mixin(PathNavigation.class)
public abstract class PathNavigationInject {
    @Shadow @Final protected Mob mob;

    @ModifyExpressionValue(method = "followThePath", at = @At(value = "CONSTANT", args = "doubleValue=0.5"))
    private double kilt$fixMC94054(double original) {
        if (original != 0.5) // mod compat
            return original;

        return ((int) (this.mob.getBbWidth() + 1)) / 2.0;
    }

    // MixinExtras beloved
    @Definition(id = "d", local = @Local(type = double.class, ordinal = 0))
    @Definition(id = "f", local = @Local(type = double.class, ordinal = 2))
    @Definition(id = "maxDistanceToWaypoint", field = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;maxDistanceToWaypoint:F")
    @Expression(value = "d < (double) this.maxDistanceToWaypoint", id = "firstComp")
    @Expression(value = "f < (double) this.maxDistanceToWaypoint", id = "secondComp")
    @WrapOperation(method = "followThePath", at = {
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "firstComp"),
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "secondComp"),
    })
    private boolean kilt$checkEqualityInComparison(double left, double right, Operation<Boolean> original) {
        return original.call(left, right) || left == right;
    }
}
