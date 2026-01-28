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

    @WrapOperation(method = "getSuitableHotbarSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z"))
    public boolean kilt$replaceWithForgePickCheck(ItemStack instance, Operation<Boolean> original, @Local(index = 1) int j) {
        return original.call(instance) && instance.isNotReplaceableByPickAction(this.player, j);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void kilt$storeZeroIndex(CallbackInfo ci, @Share("idx") LocalIntRef idx) {
        idx.set(0);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private void kilt$incrementIndex(CallbackInfo ci, @Share("idx") LocalIntRef idx) {
        idx.set(idx.get() + 1);
    }
}