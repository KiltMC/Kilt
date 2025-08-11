package xyz.bluspring.kilt.injects.world.entity.monster;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Spider.class)
public abstract class SpiderInject extends Monster {
    protected SpiderInject(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void kilt$checkIsEffectApplicable(MobEffectInstance effectInstance, CallbackInfoReturnable<Boolean> cir) {
        if (effectInstance.getEffect() == MobEffects.POISON) {
            var event = new MobEffectEvent.Applicable(this, effectInstance);
            NeoForge.EVENT_BUS.post(event);

            if (event.getResult() != Event.Result.DEFAULT) {
                cir.setReturnValue(event.getResult() == Event.Result.ALLOW);
            }
        }
    }
}
