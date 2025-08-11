package xyz.bluspring.kilt.injects.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.inventory.BrewingStandMenuInjection;

@Mixin(BrewingStandMenu.class)
public abstract class BrewingStandMenuInject {
    @ModifyExpressionValue(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V", at = @At(value = "NEW", target = "(Lnet/minecraft/world/Container;III)Lnet/minecraft/world/inventory/BrewingStandMenu$PotionSlot;"))
    private static BrewingStandMenu.PotionSlot kilt$addPotionBrewing(BrewingStandMenu.PotionSlot original, @Local PotionBrewing brewing) {
        ((BrewingStandMenuInjection.PotionSlotInjection) original).kilt$setPotionBrewing(brewing);
        return original;
    }

    @Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
    public abstract static class PotionSlotInject implements BrewingStandMenuInjection.PotionSlotInjection {
        @Shadow public static boolean mayPlaceItem(ItemStack itemStack) {
            throw new IllegalStateException();
        }

        @Unique private PotionBrewing potionBrewing = PotionBrewing.EMPTY;
        @Unique private static final ThreadLocal<PotionBrewing> kilt$potionBrewing = ThreadLocal.withInitial(() -> PotionBrewing.EMPTY);

        public PotionSlotInject(Container container, int slot, int x, int y) {}

        @CreateInitializer
        public PotionSlotInject(PotionBrewing brewing, Container container, int slot, int x, int y) {
            this(container, slot, x, y);
            this.potionBrewing = brewing;
        }

        @WrapOperation(method = "mayPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/BrewingStandMenu$PotionSlot;mayPlaceItem(Lnet/minecraft/world/item/ItemStack;)Z"))
        private boolean kilt$tryUseNeoMayPlaceItem(ItemStack itemStack, Operation<Boolean> original) {
            kilt$potionBrewing.set(this.potionBrewing);
            var result = original.call(itemStack);
            kilt$potionBrewing.remove();
            return result;
        }

        @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/BrewedPotionTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/Holder;)V"))
        private void kilt$callForgeBrewedPotionEvent(Player player, ItemStack stack, CallbackInfo ci) {
            EventHooks.onPlayerBrewedPotion(player, stack);
        }

        @ModifyReturnValue(method = "mayPlaceItem", at = @At("RETURN"))
        private static boolean kilt$tryUseBrewingResult(boolean original, @Local(argsOnly = true) ItemStack stack) {
            return kilt$potionBrewing.get().isInput(stack) || original;
        }

        @CreateStatic
        private static boolean mayPlaceItem(PotionBrewing potionBrewing, ItemStack stack) {
            kilt$potionBrewing.set(potionBrewing);
            var result = mayPlaceItem(stack);
            kilt$potionBrewing.remove();
            return result;
        }
    }
}
