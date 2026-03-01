package xyz.bluspring.kilt.mixin.compat.porting_lib.extensions;

import io.github.fabricators_of_create.porting_lib.enchant.CustomEnchantingBehaviorItem;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CustomEnchantingBehaviorItem.class)
public interface CustomEnchantingBehaviorItemMixin extends IForgeItem {
}
