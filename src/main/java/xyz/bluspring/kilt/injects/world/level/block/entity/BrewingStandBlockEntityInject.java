package xyz.bluspring.kilt.injects.world.level.block.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityInject extends BaseContainerBlockEntity {
    protected BrewingStandBlockEntityInject(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "doBrew", at = @At("HEAD"), cancellable = true)
    private static void kilt$checkAttemptBrew(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        if (EventHooks.onPotionAttemptBrew(items))
            ci.cancel();
    }

    @Definition(id = "itemStack", local = @Local(type = ItemStack.class, ordinal = 0))
    @Definition(id = "shrink", method = "Lnet/minecraft/world/item/ItemStack;shrink(I)V")
    @Expression("itemStack.shrink(1)")
    @Inject(method = "doBrew", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static void kilt$handlePotionBrewed(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        EventHooks.onPotionBrewed(items);
    }

    // Kilt: getCraftingRemainingItem handled by Fabric API

    @Definition(id = "stack", local = @Local(type = ItemStack.class, argsOnly = true))
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    @Definition(id = "POTION", field = "Lnet/minecraft/world/item/Items;POTION:Lnet/minecraft/world/item/Item;")
    @Expression("stack.is(POTION)")
    @WrapOperation(method = "canPlaceItem", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsPotionInput(ItemStack instance, Item item, Operation<Boolean> original) {
        PotionBrewing brewing = this.level != null ? this.level.potionBrewing() : PotionBrewing.EMPTY;
        return brewing.isInput(instance) || original.call(instance, item);
    }
}
