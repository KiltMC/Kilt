package xyz.bluspring.kilt.forgeinjects.world.entity.ai.control;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MoveControl.class)
public abstract class MoveControlInject {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;maxUpStep()F"))
    private float kilt$tryUseForgeUpStep(Mob instance, Operation<Float> original) {
        return instance.kilt$getStepHeight(() -> original.call(instance));
    }
}
