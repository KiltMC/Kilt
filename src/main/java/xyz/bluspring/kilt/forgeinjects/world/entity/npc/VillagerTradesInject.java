package xyz.bluspring.kilt.forgeinjects.world.entity.npc;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(VillagerTrades.class)
public abstract class VillagerTradesInject {
    @Mixin(VillagerTrades.EmeraldsForVillagerTypeItem.class)
    public abstract static class EmeraldsForVillagerTypeItemInject {
        @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"))
        private <T> boolean kilt$disableVillagerTypeValidation(Optional instance, Consumer<? super T> action) {
            return false;
        }

        @WrapOperation(method = "getOffer", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/item/ItemStack;"))
        private ItemStack kilt$checkIfTradeEmpty(ItemLike item, int count, Operation<ItemStack> original, @Cancellable CallbackInfoReturnable<MerchantOffer> cir) {
            if (item == null) {
                cir.setReturnValue(null);
                return ItemStack.EMPTY;
            }

            return original.call(item, count);
        }
    }
}
