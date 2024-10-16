// TRACKED HASH: a67a4f20572a775babd74519f3b68377454563f5
package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.mixin.world.level.block.entity.RandomizableContainerBlockEntityMixin;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityInject extends RandomizableContainerBlockEntityMixin implements WorldlyContainer {
    protected ShulkerBoxBlockEntityInject(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @ModifyReturnValue(method = "canPlaceItemThroughFace", at = @At("RETURN"))
    private boolean kilt$checkIfItemCanFitInContainerItems(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original && stack.getItem().canFitInsideContainerItems();
    }

    protected IItemHandler createUnSidedHandler() {
        return new SidedInvWrapper(this, Direction.UP);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
    }
}