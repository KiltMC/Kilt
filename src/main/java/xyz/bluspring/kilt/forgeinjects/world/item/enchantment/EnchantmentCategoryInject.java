package xyz.bluspring.kilt.forgeinjects.world.item.enchantment;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.item.enchantment.EnchantmentCategoryInjection;

import java.util.function.Predicate;

@Mixin(EnchantmentCategory.class)
public class EnchantmentCategoryInject implements EnchantmentCategoryInjection, IExtensibleEnum {
    private Predicate<Item> delegate;

    @Override
    public void setDelegate(Predicate<Item> delegate) {
        this.delegate = delegate;
    }
}
