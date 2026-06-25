package xyz.bluspring.kilt.injects.world.entity.animal.rabbit;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.rabbit.Rabbit;

@Mixin(Rabbit.class)
public abstract class RabbitInject {
    @Mixin(targets = "net.minecraft.world.entity.animal.rabbit.Rabbit$RaidGardenGoal")
    public abstract static class RaidGardenGoalInject { // Huh. Never knew this was a mechanic, the more you know I guess.
        @Shadow @Final private Rabbit rabbit;

        @Definition(id = "Boolean", type = Boolean.class)
        @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
        @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
        @Expression("(Boolean) ?.get(MOB_GRIEFING)")
        @ModifyExpressionValue(method = "canUse", at = @At("MIXINEXTRAS:EXPRESSION"))
        private Boolean kilt$checkMobGriefing(Boolean original) {
            return original || EventHooks.canEntityGrief((ServerLevel) this.rabbit.level(), this.rabbit);
        }
    }
}
