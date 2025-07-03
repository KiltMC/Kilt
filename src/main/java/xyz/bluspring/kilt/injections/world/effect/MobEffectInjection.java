package xyz.bluspring.kilt.injections.world.effect;

import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

public interface MobEffectInjection {
    default MobEffect addAttributeModifier(Holder<Attribute> attribute, ResourceLocation id, AttributeModifier.Operation operation, Int2DoubleFunction curve) {
        throw new IllegalStateException();
    }

    default void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {};

    interface AttributeTemplateInjection {
        Int2DoubleFunction curve();
        void kilt$setAttributeCurve(Int2DoubleFunction curve);
    }
}
