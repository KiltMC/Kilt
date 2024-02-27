// TRACKED HASH: b90940f2b56f4abadcf439bc0d7b45a206633f68
package xyz.bluspring.kilt.forgeinjects.client.color.item;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemColors.class)
public class ItemColorsInject {
    @Inject(at = @At("RETURN"), method = "createDefault")
    private static void kilt$initForgeItemColors(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir) {
        ForgeHooksClient.onItemColorsInit(cir.getReturnValue(), blockColors);
    }
}