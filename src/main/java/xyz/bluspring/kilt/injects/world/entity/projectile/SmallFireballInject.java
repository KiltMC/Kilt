package xyz.bluspring.kilt.injects.world.entity.projectile;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmallFireball.class)
public abstract class SmallFireballInject extends Fireball {
    public SmallFireballInject(EntityType<? extends Fireball> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "onHitBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkCanInvokeMobGriefing(boolean original, @Local Entity entity) {
        return original || EventHooks.getMobGriefingEvent(this.level(), entity);
    }
}
