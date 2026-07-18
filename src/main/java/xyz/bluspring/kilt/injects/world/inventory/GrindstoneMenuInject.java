package xyz.bluspring.kilt.injects.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.inventory.GrindstoneMenuInjection;
import xyz.bluspring.kilt.mixin.world.inventory.GrindstoneMenuAccessor;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuInject extends AbstractContainerMenu implements GrindstoneMenuInjection {
    @Shadow @Final private Container resultSlots;
    @Shadow @Final private Container repairSlots;
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
        @Shadow @Final private GrindstoneMenu this$0;

        @Shadow protected abstract int getExperienceAmount(Level level);

        @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
        private void kilt$checkGrindstoneEvent(Player player, ItemStack carried, CallbackInfo ci) {
            if (CommonHooks.onGrindstoneTake(((GrindstoneMenuAccessor) this$0).getRepairSlots(), ((GrindstoneMenuAccessor) this$0).getAccess(), player, this::getExperienceAmount))
                ci.cancel();
        }

        @Inject(method = "getExperienceAmount", at = @At("HEAD"), cancellable = true)
        private void kilt$useStoredXp(Level level, CallbackInfoReturnable<Integer> cir) {
            if (((GrindstoneMenuInjection) this$0).kilt$getXp() > -1)
                cir.setReturnValue(((GrindstoneMenuInjection) this$0).kilt$getXp());
        }
    }

    @WrapWithCondition(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 0))
    private boolean kilt$setGrindstoneChangeXp(Container instance, int i, ItemStack stack) {
        this.xp = CommonHooks.onGrindstoneChange(this.repairSlots.getItem(0), this.repairSlots.getItem(1), this.resultSlots, -1);

        return this.xp == Integer.MIN_VALUE;
    }

    @WrapOperation(method = "mergeItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamageableItem()Z"))
    private boolean kilt$checkIsRepairable(ItemStack instance, Operation<Boolean> original, @Local(name = "durability") LocalIntRef durability) {
        if (!instance.isCombineRepairable())
            durability.set(instance.getDamageValue());

        return original.call(instance) && instance.isCombineRepairable();
    }

    @Inject(method = "mergeItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V", shift = At.Shift.AFTER))
    private void kilt$setDamageValueIfIsNotRepairable(ItemStack input, ItemStack additional, CallbackInfoReturnable<ItemStack> cir, @Local(name = "newItem") ItemStack newItem) {
        if (!additional.isCombineRepairable())
            newItem.setDamageValue(additional.getDamageValue());
    }
}
