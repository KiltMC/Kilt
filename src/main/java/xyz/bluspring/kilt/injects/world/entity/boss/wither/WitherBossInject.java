package xyz.bluspring.kilt.injects.world.entity.boss.wither;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(WitherBoss.class)
public abstract class WitherBossInject extends Monster {
    protected WitherBossInject(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "Boolean", type = Boolean.class)
    @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
    @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
    @Expression("(Boolean) ?.get(MOB_GRIEFING)")
    @ModifyExpressionValue(method = "customServerAiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Boolean kilt$checkCanInvokeMobGriefing(Boolean original, @Local ServerLevel level) {
        return original || EventHooks.canEntityGrief(level, this);
    }

    @ModifyExpressionValue(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;canDestroy(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean kilt$checkCanDestroy(boolean original, @Local BlockState state, @Local BlockPos pos) {
        return original || (state.canEntityDestroy(this.level(), pos, this) && EventHooks.onEntityDestroyBlock(this, pos, state));
    }

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void kilt$checkMobDespawnEvent(CallbackInfo ci) {
        if (EventHooks.checkMobDespawn(this))
            ci.cancel();
    }
}
