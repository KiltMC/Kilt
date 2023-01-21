package xyz.bluspring.kilt.forgeinjects.world.level.entity.ai.attributes;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.entity.AttributeSupplierBuilderInjection;
import xyz.bluspring.kilt.remaps.world.entity.ai.attributes.AttributeSupplierBuilderRemap;

@Mixin(AttributeSupplier.Builder.class)
public class AttributeSupplierBuilderInject implements AttributeSupplierBuilderInjection {
    private final AttributeSupplierBuilderRemap kiltWorkaround = new AttributeSupplierBuilderRemap();

    @Override
    public boolean hasAttribute(Attribute attribute) {
        return kiltWorkaround.hasAttribute(attribute);
    }

    @Override
    public void combine(AttributeSupplier.Builder other) {
        kiltWorkaround.combine(other);
    }

    @Inject(at = @At("HEAD"), method = "build", cancellable = true)
    public void kilt$runBuildWorkaround(CallbackInfoReturnable<AttributeSupplier> cir) {
        cir.cancel();

        kiltWorkaround.build();
    }
}
