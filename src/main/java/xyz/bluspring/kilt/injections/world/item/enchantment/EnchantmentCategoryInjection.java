package xyz.bluspring.kilt.injections.world.item.enchantment;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import xyz.bluspring.kilt.mixin.EnchantmentCategoryAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

import java.util.function.Predicate;

public interface EnchantmentCategoryInjection {
    static EnchantmentCategory create(String name, Predicate<Item> delegate) {
        var value = EnumUtils.addEnumToClass(
            EnchantmentCategory.class, EnchantmentCategoryAccessor.getValues(),
                name, (size) -> EnchantmentCategoryAccessor.createEnchantmentCategory(name, size),
                (values) -> EnchantmentCategoryAccessor.setValues(values.toArray(new EnchantmentCategory[0]))
        );

        ((EnchantmentCategoryInjection) (Object) value).setDelegate(delegate);

        return value;
    }

    default Predicate<Item> getDelegate() {
        throw new IllegalStateException();
    }

    default void setDelegate(Predicate<Item> delegate) {
        throw new IllegalStateException();
    }
}
