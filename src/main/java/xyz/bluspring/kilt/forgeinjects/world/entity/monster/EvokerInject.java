package xyz.bluspring.kilt.forgeinjects.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Evoker.class)
public abstract class EvokerInject {
    @Mixin(Evoker.EvokerWololoSpellGoal.class)
    public abstract static class EvokerWololoSpellGoalInject {
        @Shadow @Final Evoker field_7268;

        @ModifyExpressionValue(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
        private boolean kilt$checkMobGriefing(boolean original) {
            return original || ForgeEventFactory.getMobGriefingEvent(field_7268.level(), field_7268);
        }
    }
}
