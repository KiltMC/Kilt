package xyz.bluspring.kilt.injects.world.entity.boss.enderdragon;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragon.class)
public abstract class EnderDragonInject extends Mob {
    // Kilt: we're not implementing the part entity patch, but we'll do everything else

    @Nullable @Unique private Player unlimitedLastHurtByPlayer = null;

    protected EnderDragonInject(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at=  @At("HEAD"))
    private void kilt$storeLastHurtByPlayer(CallbackInfo ci) {
        if (this.lastHurtByPlayer != null)
            this.unlimitedLastHurtByPlayer = this.lastHurtByPlayer;

        if (this.unlimitedLastHurtByPlayer != null && this.unlimitedLastHurtByPlayer.isRemoved())
            this.unlimitedLastHurtByPlayer = null;
    }

    @ModifyExpressionValue(method = "checkWalls", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkCanInvokeMobGriefing(boolean original) {
        return original || EventHooks.getMobGriefingEvent(this.level(), this);
    }

    @ModifyArg(method = "tickDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"))
    private int kilt$useForgeModifiedExperienceDrop(int experience) {
        return EventHooks.getExperienceDrop(this, this.unlimitedLastHurtByPlayer, experience);
    }
}
