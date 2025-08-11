// TRACKED HASH: b90940f2b56f4abadcf439bc0d7b45a206633f68
package xyz.bluspring.kilt.injects.client.color.item;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.color.item.ItemColorsInjection;

import java.util.HashMap;
import java.util.Map;

@Mixin(ItemColors.class)
public abstract class ItemColorsInject implements ItemColorsInjection {
    @Inject(at = @At("RETURN"), method = "createDefault")
    private static void kilt$initForgeItemColors(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir) {
        ClientHooks.onItemColorsInit(cir.getReturnValue(), blockColors);
    }

    @Unique
    private Map<Item, ItemColor> kilt$itemColors;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$createForgeItemColorsWorkaround(CallbackInfo ci) {
        this.kilt$itemColors = new HashMap<>();
    }

    @Override
    public Map<Item, ItemColor> kilt$getItemColors() {
        return this.kilt$itemColors;
    }

    // Kilt TODO: is this still needed?
}