package xyz.bluspring.kilt.forgeinjects.world.level.pathfinder;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AmphibiousNodeEvaluator.class)
public abstract class AmphibiousNodeEvaluatorInject {
    @WrapOperation(method = "getNeighbors", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;maxUpStep()F"))
    private float kilt$useStepHeight(Mob instance, Operation<Float> original) {
        return Math.max(instance.getStepHeight(), original.call(instance)); // Kilt: use whatever is highest honestly
    }
}
