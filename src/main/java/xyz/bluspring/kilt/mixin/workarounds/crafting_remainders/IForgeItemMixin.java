package xyz.bluspring.kilt.mixin.workarounds.crafting_remainders;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.workarounds.IForgeItemCraftingRemainderWorkaround;

@Mixin(IForgeItem.class)
public interface IForgeItemMixin {

    @Shadow
    private Item self() {
        throw new IllegalArgumentException("self() broke");
    }

    @WrapOperation(
        method = "getCraftingRemainingItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;getRecipeRemainder(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private ItemStack kilt$getCraftingRemainingItem(Item item, ItemStack stack, Operation<ItemStack> original) {
        if (!IForgeItemCraftingRemainderWorkaround.kilt$isCheckingCraftingItem.get()) {
            try {
                IForgeItemCraftingRemainderWorkaround.kilt$isCheckingCraftingItem.set(true);
                return original.call(item, stack);
            } finally {
                IForgeItemCraftingRemainderWorkaround.kilt$isCheckingCraftingItem.set(false);
            }
        }

        // Fallback to avoid stack overflow.
        if (!self().hasCraftingRemainingItem(stack)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(self().getCraftingRemainingItem());
    }
}
