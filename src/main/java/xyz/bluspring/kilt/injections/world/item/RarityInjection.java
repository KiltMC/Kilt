package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.network.chat.Style;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.UnaryOperator;

public interface RarityInjection {

    default UnaryOperator<Style> getStyleModifier() {
        throw KiltHelper.createMixinException(RarityInjection.class, "getStyleModifier");
    }

    default boolean kilt$hasCustomStyleModifier() {
        throw KiltHelper.createMixinException(RarityInjection.class, "kilt$hasCustomStyleModifier");
    }

}
