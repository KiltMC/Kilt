package xyz.bluspring.kilt.injections.world.level.block.entity;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.BlockEntityExtensions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.capabilities.ICapabilityProviderImpl;

public interface BlockEntityInjection extends ICapabilityProviderImpl<BlockEntity>, BlockEntityExtensions {
    @Override
    default void invalidateCaps() {
        ICapabilityProviderImpl.super.invalidateCaps();
    }
}
