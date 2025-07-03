// TRACKED HASH: 31fa78f22b9de5f08b03a4be3162c7c3334e121f
package xyz.bluspring.kilt.forgeinjects.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.inventory.AnvilMenuInjection;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuInject extends ItemCombinerMenu implements AnvilMenuInjection {
    @Shadow @Final private DataSlot cost;
    @Shadow private @Nullable String itemName;
    @Unique private static final ThreadLocal<Float> kilt$storedBreakChance = new ThreadLocal<>();

    public AnvilMenuInject(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 0))
    private void kilt$getForgeBreakChance(Player player, ItemStack stack, CallbackInfo ci) {
        kilt$storedBreakChance.set(CommonHooks.onAnvilRepair(player, stack, this.inputSlots.getItem(0), this.inputSlots.getItem(1)));
    }

    @ModifyExpressionValue(method = "method_24922", at = @At(value = "CONSTANT", args = "floatValue=0.12"))
    private static float kilt$useForgeBreakChanceIfPossible(float original) {
        if (original == 0.12f) {
            var value = kilt$storedBreakChance.get();
            kilt$storedBreakChance.remove();

            return value;
        }

        return original;
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1), cancellable = true)
    private void kilt$checkForgeAnvilChange(CallbackInfo ci, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 1) ItemStack stack2, @Local(ordinal = 1) int j) {
        if (!CommonHooks.onAnvilChange((AnvilMenu) (Object) this, stack, stack2, this.resultSlots, this.itemName, j, this.player))
            ci.cancel();
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamageableItem()Z"))
    private void kilt$storeEnchantedBookFlag(CallbackInfo ci, @Local boolean flag, @Share("flag") LocalBooleanRef flagRef) {
        flagRef.set(flag);
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V", ordinal = 5))
    private void kilt$checkAndSetFlag(CallbackInfo ci, @Share("flag") LocalBooleanRef flagRef, @Local(ordinal = 1) LocalRef<ItemStack> stack2, @Local(ordinal = 2) ItemStack stack3) {
        if (flagRef.get() && !stack2.get().isBookEnchantable(stack3))
            stack2.set(ItemStack.EMPTY);
    }

    @Override
    public void setMaximumCost(long value) {
        this.cost.set((int) Mth.clamp(value, 0L, Integer.MAX_VALUE));
    }
}