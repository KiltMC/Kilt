package xyz.bluspring.kilt.forgeinjects.world.entity.projectile;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LargeFireball.class)
public abstract class LargeFireballInject extends Fireball {
    public LargeFireballInject(EntityType<? extends Fireball> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkCanInvokeMobGriefing(boolean original) {
        return original || ForgeEventFactory.getMobGriefingEvent(this.level(), this.getOwner());
    }
}
