package xyz.bluspring.kilt.forgeinjects.world.level.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.item.ItemPropertiesInjection;

@Mixin(Item.class)
public class ItemInject implements IForgeItem {
    private boolean canRepair;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$setRepairability(Item.Properties properties, CallbackInfo ci) {
        canRepair = ((ItemPropertiesInjection) properties).getCanRepair();
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return isDamageable(stack) && canRepair;
    }
}
