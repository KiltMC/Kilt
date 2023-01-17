package xyz.bluspring.kilt.mixin;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(AttributeSupplier.Builder.class)
public interface AttributeSupplierBuilderAccessor {
    @Accessor
    Map<Attribute, AttributeInstance> getBuilder();

    @Accessor
    boolean isInstanceFrozen();

    @Accessor
    void setInstanceFrozen(boolean instanceFrozen);

    @Invoker
    AttributeInstance callCreate(Attribute attribute);
}
