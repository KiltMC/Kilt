package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Ravager.class)
public abstract class RavagerInject extends Raider {
    protected RavagerInject(EntityType<? extends Raider> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkMobGriefingEvent(boolean original) {
        return original && EventHooks.getMobGriefingEvent(this.level(), this);
    }
}
