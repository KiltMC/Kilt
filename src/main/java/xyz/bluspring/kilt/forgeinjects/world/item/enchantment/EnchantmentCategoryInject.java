package xyz.bluspring.kilt.forgeinjects.world.item.enchantment;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.enchantment.EnchantmentCategoryInjection;

import java.util.function.Predicate;

@Mixin(EnchantmentCategory.class)
public class EnchantmentCategoryInject implements EnchantmentCategoryInjection, IExtensibleEnum {
    @CreateStatic
    private static EnchantmentCategory create(String name, Predicate<Item> delegate) {
        return EnchantmentCategoryInjection.create(name, delegate);
    }

    private Predicate<Item> delegate;

    @Override
    public Predicate<Item> getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(Predicate<Item> delegate) {
        this.delegate = delegate;
    }
}
