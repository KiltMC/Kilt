package xyz.bluspring.kilt.injects.world.entity.ai.behavior;

import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StartAttacking.class)
public abstract class StartAttackingInject {
    @ModifyArg(method = "method_47123", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;set(Ljava/lang/Object;)V"))
    private static <Value> Value kilt$tryCallChangeTargetEvent(Value value, @Local LivingEntity entity, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        if (value instanceof LivingEntity target) {
            var event = CommonHooks.onLivingChangeTarget(entity, target, LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET);
            if (event.isCanceled() || event.getNewAboutToBeSetTarget() == null) {
                cir.setReturnValue(false);
                return null;
            }

            return (Value) event.getNewAboutToBeSetTarget();
        }

        return value;
    }
}
