// TRACKED HASH: 19d1394a69170465ae734993f6a1f2fe9f841171
package xyz.bluspring.kilt.forgeinjects.world.entity.player;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryInject {
    @Shadow @Final public NonNullList<ItemStack> armor;

    @Shadow @Final public Player player;

    @Inject(at = @At("TAIL"), method = "tick")
    public void kilt$tickArmor(CallbackInfo ci) {
        this.armor.forEach(e -> e.onArmorTick(this.player.level(), this.player));
    }

    @Redirect(method = "getSuitableHotbarSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z"))
    public boolean kilt$replaceWithForgePickCheck(ItemStack instance, @Local(index = 1) int j) {
        return instance.isNotReplaceableByPickAction(this.player, j);
    }

    @Redirect(method = "addResource(ILnet/minecraft/world/item/ItemStack;)I", at = @At(value = "NEW", target = "net/minecraft/world/item/ItemStack"))
    public ItemStack kilt$preserveStackCapabilities(ItemLike itemLike, int i, @Local(ordinal = 0) ItemStack stack) {
        var newStack = stack.copy();
        newStack.setCount(0);

        return newStack;
    }
}