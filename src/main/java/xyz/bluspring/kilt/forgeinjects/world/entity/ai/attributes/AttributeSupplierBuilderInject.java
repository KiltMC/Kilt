package xyz.bluspring.kilt.forgeinjects.world.entity.ai.attributes;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.entity.AttributeSupplierBuilderInjection;
import xyz.bluspring.kilt.mixin.AttributeSupplierAccessor;
import xyz.bluspring.kilt.mixin.AttributeSupplierBuilderAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(AttributeSupplier.Builder.class)
public abstract class AttributeSupplierBuilderInject implements AttributeSupplierBuilderInjection {
    private List<AttributeSupplier.Builder> others = new ArrayList<>();

    @Shadow @Final private Map<Attribute, AttributeInstance> builder;

    @Shadow private boolean instanceFrozen;

    @Override
    public void combine(AttributeSupplier.Builder other) {
        this.builder.putAll(((AttributeSupplierBuilderAccessor) other).getBuilder());
        others.add(other);
    }

    @Override
    public boolean hasAttribute(Attribute attribute) {
        return this.builder.containsKey(attribute);
    }

    @Inject(at = @At("HEAD"), method = "build", cancellable = true)
    public void kilt$build(CallbackInfoReturnable<AttributeSupplier> cir) {
        this.instanceFrozen = true;
        for (AttributeSupplier.Builder other : others) {
            ((AttributeSupplierBuilderAccessor) other).setInstanceFrozen(true);
        }

        cir.setReturnValue(new AttributeSupplier(this.builder));
    }

    public AttributeSupplierBuilderInject() {}

    @CreateInitializer
    public AttributeSupplierBuilderInject(AttributeSupplier attributeMap) {
        this();

        this.builder.putAll(((AttributeSupplierAccessor) attributeMap).getInstances());
    }
}
