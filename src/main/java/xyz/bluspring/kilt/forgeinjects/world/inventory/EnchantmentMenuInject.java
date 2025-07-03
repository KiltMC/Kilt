package xyz.bluspring.kilt.forgeinjects.world.inventory;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuInject {
    @Shadow @Final public int[] costs;

    /*@Mixin(targets = "net.minecraft.world.inventory.EnchantmentMenu$2")
    public abstract static class AnonymousEnchantFuelSlotInject {
        @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
        private boolean kilt$checkIsValidEnchantingFuel(boolean original, @Local(argsOnly = true) ItemStack stack) {
            return original || stack.is(Tags.Items.ENCHANTING_FUELS);
        }
    }*/

    @ModifyExpressionValue(method = "method_17411", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/EnchantingTableBlock;isValidBookShelf(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$addEnchantPowerBonus(boolean original, ItemStack itemStack, Level level, BlockPos blockPos, @Share("enchantPowerBonus") LocalFloatRef enchantPowerBonusRef, @Local(ordinal = 1) BlockPos pos2) {
        if (original) {
            enchantPowerBonusRef.set(enchantPowerBonusRef.get() + level.getBlockState(blockPos.offset(pos2)).getEnchantPowerBonus(level, blockPos.offset(pos2)));
        }

        return original;
    }

    @ModifyArg(method = "method_17411", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"), index = 1)
    private int kilt$useForgeEnchantBonus(int enchantBonus, @Share("enchantPowerBonus") LocalFloatRef enchantPowerBonusRef) {
        return (int) enchantPowerBonusRef.get();
    }

    @Definition(id = "costs", field = "Lnet/minecraft/world/inventory/EnchantmentMenu;costs:[I")
    @Expression("this.costs[?] < ? + 1")
    @ModifyExpressionValue(method = "method_17411", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$callForgeSetEnchantmentLevelEvent(boolean original, @Local(ordinal = 1) int k, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos, @Share("enchantPowerBonus") LocalFloatRef enchantPowerBonusRef, @Local(ordinal = 0) ItemStack stack) {
        if (original) {
            this.costs[k] = EventHooks.onEnchantmentLevelSet(level, pos, k, (int) enchantPowerBonusRef.get(), stack, 0);
            return false;
        } else {
            this.costs[k] = EventHooks.onEnchantmentLevelSet(level, pos, k, (int) enchantPowerBonusRef.get(), stack, this.costs[k]);
        }

        return original;
    }

    // TODO: onPlayerEnchantItem

    /*@WrapOperation(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0))
    private boolean kilt$checkIsEnchantingFuel(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.is(Tags.Items.ENCHANTING_FUELS);
    }*/
}
