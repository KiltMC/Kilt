package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TurtleEggBlock.class)
public abstract class TurtleEggBlockInject {
    @ModifyExpressionValue(method = "canDestroyEgg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkMobGriefing(boolean original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) Entity entity) {
        return original && EventHooks.canEntityGrief(level, entity);
    }
}
