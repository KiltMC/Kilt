package xyz.bluspring.kilt.injections.world.item.enchantment;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.UnaryOperator;

public interface EnchantmentBuilderInjection {
    default Enchantment.Builder withCustomName(UnaryOperator<MutableComponent> nameFactory) {
        throw KiltHelper.createMixinException(EnchantmentBuilderInjection.class, "withCustomName");
    }
}
