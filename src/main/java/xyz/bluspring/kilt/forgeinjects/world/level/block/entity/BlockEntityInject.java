package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import io.github.fabricators_of_create.porting_lib.extensions.BlockEntityExtensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.capabilities.BlockEntityCapabilityProviderImpl;

@Mixin(BlockEntity.class)
@Extends(CapabilityProvider.class)
public class BlockEntityInject implements IForgeBlockEntity, CapabilityProviderInjection, BlockEntityCapabilityProviderImpl, BlockEntityExtensions {
    @Override
    public CompoundTag getPersistentData() {
        return null;
    }
}
