package xyz.bluspring.kilt.injects.world.entity.boss.enderdragon;

import com.llamalad7.mixinextras.expression.Definition;import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(EnderDragon.class)
public abstract class EnderDragonInject extends Mob {
    // Kilt: we're not implementing the part entity patch, but we'll do everything else

    @Nullable @Unique private EntityReference<Player> unlimitedLastHurtByPlayer = null;

    protected EnderDragonInject(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at=  @At("HEAD"))
    private void kilt$storeLastHurtByPlayer(CallbackInfo ci) {
        if (this.lastHurtByPlayer != null)
            this.unlimitedLastHurtByPlayer = this.lastHurtByPlayer;

        if (this.unlimitedLastHurtByPlayer != null) {
            var player = this.unlimitedLastHurtByPlayer.getEntity(this.level(), Player.class);

            if (player == null || player.isRemoved()) {
                this.unlimitedLastHurtByPlayer = null;
            }
        }
    }

    @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
    @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
    @Definition(id = "Boolean", type = Boolean.class)
    @Expression("(Boolean) ?.get(MOB_GRIEFING)")
    @ModifyExpressionValue(method = "checkWalls", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Boolean kilt$checkCanInvokeMobGriefing(Boolean original, @Local ServerLevel level) {
        return original || EventHooks.canEntityGrief(level, this);
    }

    @ModifyArg(method = "tickDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"))
    private int kilt$useForgeModifiedExperienceDrop(int experience, @Local ServerLevel level) {
        return EventHooks.getExperienceDrop(this, EntityReference.get(this.unlimitedLastHurtByPlayer, level, Player.class), experience);
    }
}
