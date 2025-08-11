package xyz.bluspring.kilt.injects.world.damagesource;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.IDeathMessageProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.damagesource.DeathMessageTypeInjection;

@Mixin(CombatTracker.class)
public abstract class CombatTrackerInject {
    @Shadow @Final private LivingEntity mob;

    @Definition(id = "deathMessageType", local = @Local(type = DeathMessageType.class))
    @Definition(id = "FALL_VARIANTS", field = "Lnet/minecraft/world/damagesource/DeathMessageType;FALL_VARIANTS:Lnet/minecraft/world/damagesource/DeathMessageType;")
    @Expression("deathMessageType == FALL_VARIANTS")
    @Inject(method = "getDeathMessage", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$useNeoDeathMessage(CallbackInfoReturnable<Component> cir, @Local DeathMessageType deathMessageType, @Local(ordinal = 0) CombatEntry combatEntry, @Local(ordinal = 1) CombatEntry mostSignificantFall) {
        var messageFunction = ((DeathMessageTypeInjection) (Object) deathMessageType).getMessageFunction();
        if (messageFunction != IDeathMessageProvider.DEFAULT) {
            cir.setReturnValue(messageFunction.getDeathMessage(this.mob, combatEntry, mostSignificantFall));
        }
    }
}
