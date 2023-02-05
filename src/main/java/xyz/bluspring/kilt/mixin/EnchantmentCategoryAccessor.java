package xyz.bluspring.kilt.mixin;

import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnchantmentCategory.class)
public interface EnchantmentCategoryAccessor {
    @Invoker("<init>")
    static EnchantmentCategory createEnchantmentCategory(String name, int id) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static EnchantmentCategory[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static void setValues(EnchantmentCategory[] values) {
        throw new IllegalStateException();
    }
}
