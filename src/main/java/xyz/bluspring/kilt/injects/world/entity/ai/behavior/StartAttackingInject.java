package xyz.bluspring.kilt.injects.world.entity.ai.behavior;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;

@Mixin(StartAttacking.class)
public abstract class StartAttackingInject {
    @Definition(id = "attackTarget", local = @Local(type = MemoryAccessor.class, name = "attackTarget", argsOnly = true))
    @Definition(id = "set", method = "Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;set(Ljava/lang/Object;)V")
    @Definition(id = "targetEntity", local = @Local(type = LivingEntity.class, name = "targetEntity"))
    @Expression("attackTarget.set(@(targetEntity))")
    @ModifyExpressionValue(method = "lambda$create$3", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static <E extends Mob> LivingEntity kilt$tryCallChangeTargetEvent(LivingEntity original, @Local(name = "body", argsOnly = true) E body, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        var event = CommonHooks.onLivingChangeTarget(body, original, LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET);
        if (event.isCanceled() || event.getNewAboutToBeSetTarget() == null) {
            cir.setReturnValue(false);
            return null;
        }

        return event.getNewAboutToBeSetTarget();
    }
}
