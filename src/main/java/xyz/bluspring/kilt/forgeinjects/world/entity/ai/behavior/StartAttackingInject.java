package xyz.bluspring.kilt.forgeinjects.world.entity.ai.behavior;

import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StartAttacking.class)
public abstract class StartAttackingInject {
    @ModifyArg(method = "method_47123", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;set(Ljava/lang/Object;)V"))
    private static <Value> Value kilt$changeLivingTarget(Value value, @Local(argsOnly = true) Mob entity, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        var event = ForgeHooks.onLivingChangeTarget(entity, (LivingEntity) value, LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET);

        if (event.isCanceled()) {
            cir.setReturnValue(false);
            return null;
        }

        return (Value) event.getNewTarget();
    }
}
