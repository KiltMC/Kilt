// TRACKED HASH: 77d21c29ec548ed8806ff46ca5b80c8a57b615ce
package xyz.bluspring.kilt.injects.world.effect;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.common.extensions.IMobEffectExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.effect.MobEffectInjection;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(MobEffect.class)
public abstract class MobEffectInject implements MobEffectInjection, IMobEffectExtension {
    @Shadow @Final private Map<Holder<Attribute>, MobEffect.AttributeTemplate> attributeModifiers;

    @Override
    public MobEffect addAttributeModifier(Holder<Attribute> attribute, ResourceLocation id, AttributeModifier.Operation operation, Int2DoubleFunction curve) {
        var template = new MobEffect.AttributeTemplate(id, curve.apply(0), operation);
        ((MobEffectInjection.AttributeTemplateInjection) template).kilt$setAttributeCurve(curve);
        this.attributeModifiers.put(attribute, template);
        return (MobEffect) (Object) this;
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
    }

    @Mixin(MobEffect.AttributeTemplate.class)
    public abstract static class AttributeTemplateInject implements MobEffectInjection.AttributeTemplateInjection {
        @Unique private Int2DoubleFunction curve = null;

        public AttributeTemplateInject(ResourceLocation id, double amount, AttributeModifier.Operation operation) {}

        @CreateInitializer
        public AttributeTemplateInject(ResourceLocation id, double amount, AttributeModifier.Operation operation, @Nullable Int2DoubleFunction curve) {
            this(id, amount, operation);
            this.curve = curve;
        }

        @Override
        public Int2DoubleFunction curve() {
            return this.curve;
        }

        @Override
        public void kilt$setAttributeCurve(Int2DoubleFunction curve) {
            this.curve = curve;
        }

        @ModifyArg(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;<init>(Lnet/minecraft/resources/ResourceLocation;DLnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;)V"))
        private double kilt$useNeoAttributeCurve(double original, @Local(argsOnly = true) int level) {
            if (this.curve != null) {
                return this.curve.apply(level);
            }

            return original;
        }
    }
}