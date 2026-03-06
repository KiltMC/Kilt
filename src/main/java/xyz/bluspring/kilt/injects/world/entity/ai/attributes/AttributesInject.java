package xyz.bluspring.kilt.injects.world.entity.ai.attributes;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.common.PercentageAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Attributes.class)
public abstract class AttributesInject {
    // Kilt TODO: how can we make this better please
    @Definition(id = "RangedAttribute", type = RangedAttribute.class)
    @Expression("new RangedAttribute('attribute.name.generic.knockback_resistance', ?, ?, ?)")
    @WrapOperation(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static RangedAttribute kilt$usePercentageAttributeForKnockbackResistance(String descriptionId, double defaultValue, double min, double max, Operation<RangedAttribute> original) {
        return new PercentageAttribute(descriptionId, defaultValue, min, max);
    }

    @Definition(id = "RangedAttribute", type = RangedAttribute.class)
    @Expression("new RangedAttribute('attribute.name.generic.movement_speed', ?, ?, ?)")
    @WrapOperation(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static RangedAttribute kilt$usePercentageAttributeForMovementSpeed(String descriptionId, double defaultValue, double min, double max, Operation<RangedAttribute> original) {
        return new PercentageAttribute(descriptionId, defaultValue, min, max);
    }
}
