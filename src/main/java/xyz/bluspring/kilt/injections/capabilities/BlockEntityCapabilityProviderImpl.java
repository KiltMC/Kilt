package xyz.bluspring.kilt.injections.capabilities;

import io.github.fabricators_of_create.porting_lib.extensions.BlockEntityExtensions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;

public interface BlockEntityCapabilityProviderImpl extends ICapabilityProviderImpl<BlockEntity>, BlockEntityExtensions {
    @Override
    default void invalidateCaps() {
        ICapabilityProviderImpl.super.invalidateCaps();
    }
}
