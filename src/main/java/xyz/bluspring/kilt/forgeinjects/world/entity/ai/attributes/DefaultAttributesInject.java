// TRACKED HASH: 04ed1ed8c5f75415ffb7509faccf5a3e9d757b25
package xyz.bluspring.kilt.forgeinjects.world.entity.ai.attributes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultAttributes.class)
public class DefaultAttributesInject {
    @Inject(at = @At("TAIL"), method = "getSupplier", cancellable = true)
    private static void kilt$useForgeSupplier(EntityType<? extends LivingEntity> entityType, CallbackInfoReturnable<AttributeSupplier> cir) {
        var supplier = ForgeHooks.getAttributesView().get(entityType);
        if (supplier != null)
            cir.setReturnValue(supplier);
    }

    @Inject(at = @At("TAIL"), method = "hasSupplier", cancellable = true)
    private static void kilt$hasForgeSupplier(EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir) {
        if (ForgeHooks.getAttributesView().containsKey(entityType))
            cir.setReturnValue(true);
    }
}