// TRACKED HASH: 790222e880a2fb721fe089f49d812d7025836f0a
package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.DyeColorInjection;

@Mixin(DyeColor.class)
public class DyeColorInject implements DyeColorInjection {
    @CreateStatic
    private static DyeColor getColor(ItemStack stack) {
        return DyeColorInjection.getColor(stack);
    }
}