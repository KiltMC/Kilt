package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
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
}
