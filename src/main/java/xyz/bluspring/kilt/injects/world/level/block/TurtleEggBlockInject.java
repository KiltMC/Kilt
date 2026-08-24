package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.TurtleEggBlock;

@Mixin(TurtleEggBlock.class)
public abstract class TurtleEggBlockInject {
    @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
    @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
    @Definition(id = "Boolean", type = Boolean.class)
    @Expression("(Boolean) ?.get(MOB_GRIEFING)")
    @ModifyExpressionValue(method = "canDestroyEgg", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Boolean kilt$checkMobGriefing(Boolean original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) Entity entity) {
        return original && EventHooks.canEntityGrief(level, entity);
    }
}
