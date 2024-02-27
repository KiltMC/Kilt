// TRACKED HASH: ad9e0035fc910cb9d79f74951b83b361e6bd1ba9
package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.RarityInjection;

import java.util.function.UnaryOperator;

@Mixin(Rarity.class)
public class RarityInject implements RarityInjection, IExtensibleEnum {
    private UnaryOperator<Style> styleModifier;

    @Override
    public UnaryOperator<Style> getStyleModifier() {
        return styleModifier;
    }

    @Override
    public void setStyleModifier(UnaryOperator<Style> styleModifier) {
        this.styleModifier = styleModifier;
    }

    @CreateStatic
    private static Rarity create(String name, ChatFormatting formatting) {
        return RarityInjection.create(name, formatting);
    }

    @CreateStatic
    private static Rarity create(String name, UnaryOperator<Style> styleModifier) {
        return RarityInjection.create(name, styleModifier);
    }
}