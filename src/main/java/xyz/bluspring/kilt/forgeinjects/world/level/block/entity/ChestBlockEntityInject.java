// TRACKED HASH: cd9795e4ea5e202de00de0ff0c249e05e8b79e60
package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import com.bawnorton.mixinsquared.TargetHandler;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChestBlockEntity.class, priority = 1010) // higher priority to surpass Lithium
public abstract class ChestBlockEntityInject extends RandomizableContainerBlockEntity {
    @Unique
    private LazyOptional<IItemHandlerModifiable> chestHandler;

    protected ChestBlockEntityInject(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    // Lithium has an intrinsic that I cannot figure out how to override, so this works for now.
    @IfModAbsent("lithium")
    public void setBlockState(BlockState blockState) {
        super.setBlockState(blockState);
        if (this.chestHandler != null) {
            var oldHandler = this.chestHandler;
            this.chestHandler = null;
            oldHandler.invalidate();
        }
    }

    @IfModLoaded("lithium")
    @TargetHandler(mixin = "me.jellysquid.mods.lithium.mixin.util.inventory_change_listening.ChestBlockEntityMixin", name = "emitRemovedOnSetCachedState")
    @Inject(method = "@MixinSquared:Handler", at = @At("TAIL"))
    private void kilt$invalidateChestHandlerLithium(CallbackInfo ci) {
        if (this.chestHandler != null) {
            var oldHandler = this.chestHandler;
            this.chestHandler = null;
            oldHandler.invalidate();
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && !this.remove) {
            if (this.chestHandler == null)
                this.chestHandler = LazyOptional.of(this::createHandler);

            return this.chestHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Unique
    private IItemHandlerModifiable createHandler() {
        var state = this.getBlockState();
        if (!(state.getBlock() instanceof ChestBlock chestBlock)) {
            return new InvWrapper(this);
        }

        var inv = ChestBlock.getContainer(chestBlock, state, this.getLevel(), this.getBlockPos(), true);
        return new InvWrapper(inv == null ? this : inv);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (chestHandler != null) {
            chestHandler.invalidate();
            chestHandler = null;
        }
    }
}