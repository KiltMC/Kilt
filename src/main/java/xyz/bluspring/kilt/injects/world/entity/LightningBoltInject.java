// TRACKED HASH: 171de07c400ad7a7aa28f0ddfa4684a0fda24e32
package xyz.bluspring.kilt.injects.world.entity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.entity.LightningBoltInjection;

@Mixin(LightningBolt.class)
public abstract class LightningBoltInject implements LightningBoltInjection {
    @Unique private float damage = 5.0f;

    @Override
    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public float getDamage() {
        return this.damage;
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;thunderHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LightningBolt;)V"))
    private boolean kilt$checkEntityStruckByLightning(Entity instance, ServerLevel level, LightningBolt lightning) {
        return !EventHooks.onEntityStruckByLightning(instance, lightning);
    }
}