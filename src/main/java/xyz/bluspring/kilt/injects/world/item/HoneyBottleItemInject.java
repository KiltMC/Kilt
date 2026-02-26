package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoneyBottleItem;
import net.neoforged.neoforge.common.EffectCures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoneyBottleItem.class)
public abstract class HoneyBottleItemInject {
    @WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean kilt$removeHoneyCureEffects(LivingEntity instance, Holder<MobEffect> effect, Operation<Boolean> original) {
        instance.removeEffectsCuredBy(EffectCures.HONEY);
        return original.call(instance, effect);
    }
}
