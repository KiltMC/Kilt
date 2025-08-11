// TRACKED HASH: 19d1394a69170465ae734993f6a1f2fe9f841171
package xyz.bluspring.kilt.injects.world.entity.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(Inventory.class)
public abstract class InventoryInject {
    @Shadow @Final public NonNullList<ItemStack> armor;

    @Shadow @Final public Player player;

    @Shadow public int selected;

    @Inject(at = @At("TAIL"), method = "tick")
    public void kilt$tickArmor(CallbackInfo ci) {
        this.armor.forEach(e -> e.onArmorTick(this.player.level(), this.player));
    }

    @WrapOperation(method = "getSuitableHotbarSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z"))
    public boolean kilt$replaceWithForgePickCheck(ItemStack instance, Operation<Boolean> original, @Local(index = 1) int j) {
        return original.call(instance) && instance.isNotReplaceableByPickAction(this.player, j);
    }

    @Redirect(method = "addResource(ILnet/minecraft/world/item/ItemStack;)I", at = @At(value = "NEW", target = "net/minecraft/world/item/ItemStack"))
    public ItemStack kilt$preserveStackCapabilities(ItemLike itemLike, int i, @Local(ordinal = 0) ItemStack stack) {
        var newStack = stack.copy();
        newStack.setCount(0);

        return newStack;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void kilt$storeZeroIndex(CallbackInfo ci, @Share("idx") LocalIntRef idx) {
        idx.set(0);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private void kilt$incrementIndex(CallbackInfo ci, @Share("idx") LocalIntRef idx) {
        idx.set(idx.get() + 1);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"))
    private void kilt$callForgeInventoryTick(ItemStack instance, Level level, Entity entity, int inventorySlot, boolean isCurrentItem, Operation<Void> original, @Share("idx") LocalIntRef idx) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getItem().getClass(), Item.class, "onInventoryTick", ItemStack.class, Level.class, Player.class, int.class, int.class)) {
            instance.onInventoryTick(level, (Player) entity, idx.get() - 1, this.selected);
        } else {
            original.call(instance, level, entity, inventorySlot, isCurrentItem);
        }
    }
}