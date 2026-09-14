// TRACKED HASH: 2722c364d3008892dbf935bf61e6a4457d6180e7
package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.HashMap;
import java.util.Map;

@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityInject extends BlockEntity {
    @Unique private LazyOptional<?> itemHandler = LazyOptional.of(() -> createUnSidedHandler());

    @Unique
    private Map<Direction, LazyOptional<?>> kilt$sidedItemHandlers = null;

    public BaseContainerBlockEntityInject(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void kilt$init(BlockEntityType<?> type, BlockPos pos, BlockState blockState, CallbackInfo ci) {
        if (
            this instanceof WorldlyContainer container &&
            !KiltHelper.INSTANCE.hasMethodOverride(
                this.getClass(), BaseContainerBlockEntity.class, "getCapability",
                Capability.class, Direction.class
            ) &&
            !KiltHelper.INSTANCE.hasMethodOverride(
                this.getClass(), ICapabilityProvider.class, "getCapability",
                Capability.class
            )
        ) {
            kilt$sidedItemHandlers = new HashMap<>();
            kilt$prepareSidedHandlers(container);
        }
    }

    @Unique
    private void kilt$prepareSidedHandlers(WorldlyContainer container) {
        for (var direction : Direction.values()) {
            kilt$sidedItemHandlers.put(direction, LazyOptional.of(() -> new SidedInvWrapper(container, direction)));
        }
    }

    protected IItemHandler createUnSidedHandler() {
        return new InvWrapper((BaseContainerBlockEntity) (Object) this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && !this.remove) {
            if (side != null && kilt$sidedItemHandlers != null) {
                return kilt$sidedItemHandlers.get(side).cast();
            }
            return itemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        if (kilt$sidedItemHandlers != null) {
            kilt$sidedItemHandlers.values().forEach(LazyOptional::invalidate);
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandler = LazyOptional.of(this::createUnSidedHandler);
        if (kilt$sidedItemHandlers != null && this instanceof WorldlyContainer container) {
            kilt$prepareSidedHandlers(container);
        }
    }
}
