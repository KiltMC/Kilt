// TRACKED HASH: 31fa78f22b9de5f08b03a4be3162c7c3334e121f
package xyz.bluspring.kilt.injects.world.inventory;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.common.CommonHooks;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.inventory.AnvilMenuInjection;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuInject extends ItemCombinerMenu implements AnvilMenuInjection {
    @Shadow @Final private DataSlot cost;
    @Shadow private @Nullable String itemName;

    public AnvilMenuInject(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition itemInputSlots) {
        super(menuType, containerId, inventory, access, itemInputSlots);
    }

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void kilt$fireAnvilCraftPreEvent(Player player, ItemStack carried, CallbackInfo ci, @Share("leftInputSnapshot") LocalRef<ItemStack> leftInputSnapshot, @Share("rightInputSnapshot") LocalRef<ItemStack> rightInputSnapshot) {
        var event = CommonHooks.fireAnvilCraftPre((AnvilMenu) (Object) this, player, carried, this.inputSlots.getItem(0), this.inputSlots.getItem(1));
        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        leftInputSnapshot.set(this.inputSlots.getItem(0).copy());
        rightInputSnapshot.set(this.inputSlots.getItem(1).copy());
    }

    @Inject(method = "onTake", at = @At("TAIL"))
    private void kilt$fireAnvilCraftPostEvent(Player player, ItemStack carried, CallbackInfo ci, @Share("leftInputSnapshot") LocalRef<ItemStack> leftInputSnapshot, @Share("rightInputSnapshot") LocalRef<ItemStack> rightInputSnapshot) {
        CommonHooks.fireAnvilCraftPost((AnvilMenu) (Object) this, player, carried, leftInputSnapshot.get(), rightInputSnapshot.get());
    }

    // Kilt TODO: use the template generator thing to add onAnvilUpdate
    @Override
    public void kilt$handleUpdateEvent() {
        ItemStack leftInput = this.inputSlots.getItem(0);
        ItemStack rightInput = this.inputSlots.getItem(1);

        CommonHooks.onAnvilUpdate((AnvilMenu) (Object) this, leftInput, rightInput, this.resultSlots, this.itemName, this.player);
    }

    @WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean kilt$checkSupportsEnchantment(Enchantment instance, ItemStack itemStack, Operation<Boolean> original, @Local(name = "enchantmentHolder") Holder<Enchantment> enchantmentHolder) {
        return original.call(instance, itemStack) || itemStack.supportsEnchantment(enchantmentHolder);
    }

    @Override
    public void setCost(int value) {
        this.cost.set(Math.max(0, value));
    }
}
