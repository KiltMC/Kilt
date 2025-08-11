package xyz.bluspring.kilt.injects.world.entity.ai.goal;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RunAroundLikeCrazyGoal.class)
public abstract class RunAroundLikeCrazyGoalInject extends Goal {
    @Shadow @Final private AbstractHorse horse;

    @Definition(id = "horse", field = "Lnet/minecraft/world/entity/ai/goal/RunAroundLikeCrazyGoal;horse:Lnet/minecraft/world/entity/animal/horse/AbstractHorse;")
    @Definition(id = "getRandom", method = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;getRandom()Lnet/minecraft/util/RandomSource;")
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Expression("this.horse.getRandom().nextInt(?) < ?")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanTameHorse(boolean original, @Local Entity entity) {
        return original && !EventHooks.onAnimalTame(this.horse, (Player) entity);
    }
}
