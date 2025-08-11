package xyz.bluspring.kilt.injects.world.entity.ai.behavior;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Swim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Swim.class)
public abstract class SwimInject {
    @ModifyReturnValue(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;)Z", at = @At("RETURN"))
    private boolean kilt$tryCheckFluidType(boolean original, @Local(argsOnly = true) Mob mob) {
        return original || mob.isInFluidType((fluidType, height) -> mob.canSwimInFluidType(fluidType) && height > mob.getFluidJumpThreshold());
    }
}
