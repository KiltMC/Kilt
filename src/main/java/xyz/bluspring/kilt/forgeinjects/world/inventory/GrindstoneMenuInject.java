package xyz.bluspring.kilt.forgeinjects.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.item.enchantment.EnchantmentHelperInjection;
import xyz.bluspring.kilt.injections.world.inventory.GrindstoneMenuInjection;
import xyz.bluspring.kilt.mixin.world.inventory.GrindstoneMenuAccessor;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuInject extends AbstractContainerMenu implements GrindstoneMenuInjection {
    @Shadow @Final private Container resultSlots;
    @Unique private int xp = -1;

    protected GrindstoneMenuInject(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Override
    public int kilt$getXp() {
        return xp;
    }

    @Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$2")
    public abstract static class AnonymousRepairSlot0Inject {
        @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
        private boolean kilt$checkCanGrindstoneRepair(boolean original, ItemStack stack) {
            return original || stack.canGrindstoneRepair();
        }
    }

    @Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$3")
    public abstract static class AnonymousRepairSlot1Inject {
        @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
        private boolean kilt$checkCanGrindstoneRepair(boolean original, ItemStack stack) {
            return original || stack.canGrindstoneRepair();
        }
    }

    @Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
    public abstract static class AnonymousResultSlotInject {
        @Shadow @Final private GrindstoneMenu field_16780;

        @Shadow protected abstract int getExperienceAmount(Level level);

        @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
        private void kilt$checkGrindstoneEvent(Player player, ItemStack stack, CallbackInfo ci) {
            if (ForgeHooks.onGrindstoneTake(((GrindstoneMenuAccessor) field_16780).getRepairSlots(), ((GrindstoneMenuAccessor) field_16780).getAccess(), this::getExperienceAmount))
                ci.cancel();
        }

        @Inject(method = "getExperienceAmount", at = @At("HEAD"), cancellable = true)
        private void kilt$useStoredXp(Level level, CallbackInfoReturnable<Integer> cir) {
            if (((GrindstoneMenuInjection) field_16780).kilt$getXp() > -1)
                cir.setReturnValue(((GrindstoneMenuInjection) field_16780).kilt$getXp());
        }
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0), cancellable = true)
    private void kilt$setGrindstoneChangeXp(CallbackInfo ci, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 1) ItemStack stack2) {
        this.xp = ForgeHooks.onGrindstoneChange(stack, stack2, this.resultSlots, -1);

        if (this.xp != Integer.MIN_VALUE) {
            this.broadcastChanges();
            ci.cancel();
        }
    }

    @WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamageableItem()Z"))
    private boolean kilt$checkIsRepairable(ItemStack instance, Operation<Boolean> original, @Local(ordinal = 0) LocalIntRef i) {
        if (!instance.isRepairable())
            i.set(instance.getDamageValue());

        return original.call(instance) && instance.isRepairable();
    }

    @WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/GrindstoneMenu;removeNonCurses(Lnet/minecraft/world/item/ItemStack;II)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack kilt$skipRepairIfCountUnobtainable(GrindstoneMenu instance, ItemStack stack, int damage, int count, Operation<ItemStack> original) {
        if (count > stack.getMaxStackSize())
            return ItemStack.EMPTY;
        else
            return original.call(instance, stack, damage, count);
    }

    @WrapOperation(method = "mergeEnchants", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I"))
    private int kilt$checkTagEnchantmentLevel(Enchantment enchantment, ItemStack stack, Operation<Integer> original) {
        if (EnchantmentHelperInjection.getTagEnchantmentLevel(enchantment, stack) == 0) {
            return 0;
        }

        return original.call(enchantment, stack);
    }
}
