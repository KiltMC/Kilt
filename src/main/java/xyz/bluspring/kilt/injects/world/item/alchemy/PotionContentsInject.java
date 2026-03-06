package xyz.bluspring.kilt.injects.world.item.alchemy;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.common.util.AttributeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.IteratorWrapper;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Iterator;
import java.util.function.Consumer;

@Mixin(PotionContents.class)
public abstract class PotionContentsInject {
    @ModifyExpressionValue(method = "addPotionTooltip(Ljava/lang/Iterable;Ljava/util/function/Consumer;FF)V", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private static <E> Iterator<E> kilt$addAttributesToPotionTooltip(Iterator<E> original, @Local(argsOnly = true) Consumer<Component> tooltipAdder) {
        return new IteratorWrapper<>(original, value -> {
            if (value instanceof Pair) {
                var pair = ((Pair<Holder<Attribute>, AttributeModifier>) value);
                if (KiltHelper.INSTANCE.hasMethodOverride(pair.getFirst().value().getClass(), Attribute.class, "toComponent", AttributeModifier.class, TooltipFlag.class)) {
                    tooltipAdder.accept(pair.getFirst().value().toComponent(pair.getSecond(), AttributeUtil.getTooltipFlag()));
                    return null;
                }
            }

            return value;
        });
    }
}
