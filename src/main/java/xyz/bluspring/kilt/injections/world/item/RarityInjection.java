package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public interface RarityInjection {

    default UnaryOperator<Style> getStyleModifier() {
        throw new IllegalStateException();
    }

}
