package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import xyz.bluspring.kilt.mixin.RarityAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

import java.util.function.UnaryOperator;

public interface RarityInjection {
    static Rarity create(String name, ChatFormatting formatting) {
        var value = EnumUtils.addEnumToClass(
                Rarity.class, RarityAccessor.getValues(),
                name, (size) -> RarityAccessor.createRarity(name, size, formatting),
                (values) -> RarityAccessor.setValues(values.toArray(new Rarity[0]))
        );
        ((RarityInjection) (Object) value).setStyleModifier((style) -> style.applyFormat(formatting));

        return value;
    }

    static Rarity create(String name, UnaryOperator<Style> styleModifier) {
        var value = create(name, ChatFormatting.BLACK);
        ((RarityInjection) (Object) value).setStyleModifier(styleModifier);

        return value;
    }

    default UnaryOperator<Style> getStyleModifier() {
        throw new IllegalStateException();
    }

    default void setStyleModifier(UnaryOperator<Style> styleModifier) {
        throw new IllegalStateException();
    }
}
