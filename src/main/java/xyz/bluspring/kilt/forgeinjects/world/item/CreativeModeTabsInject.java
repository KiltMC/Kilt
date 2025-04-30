package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(CreativeModeTabs.class)
public abstract class CreativeModeTabsInject {
    @WrapOperation(method = {"method_48951", "method_48946"}, at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private static boolean kilt$checkBookAllowedInCreativeTab(Set<EnchantmentCategory> instance, Object o, Operation<Boolean> original, @Local(argsOnly = true) Enchantment enchantment) {
        return original.call(instance, o) && enchantment.allowedInCreativeTab(Items.ENCHANTED_BOOK, instance);
    }
}
