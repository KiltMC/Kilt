package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MilkBucketItem;
import net.neoforged.neoforge.common.EffectCures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemInject {
    @Redirect(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private boolean kilt$tryCureViaMilk(LivingEntity instance) { // Kilt TODO: how to make more mod compatible?
        return instance.removeEffectsCuredBy(EffectCures.MILK);
    }
}
