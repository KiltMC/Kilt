package xyz.bluspring.kilt.forgeinjects.world.entity.animal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Rabbit.class)
public abstract class RabbitInject {
    @Mixin(targets = "net.minecraft.world.entity.animal.Rabbit$RaidGardenGoal")
    public abstract static class RaidGardenGoalInject { // Huh. Never knew this was a mechanic, the more you know I guess.
        @Shadow @Final private Rabbit rabbit;

        @ModifyExpressionValue(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
        private boolean kilt$checkMobGriefing(boolean original) {
            return original || ForgeEventFactory.getMobGriefingEvent(this.rabbit.level(), this.rabbit);
        }
    }
}
