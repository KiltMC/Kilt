package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import io.github.fabricators_of_create.porting_lib.extensions.BlockEntityExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.workarounds.CapabilityInvalidationWorkaround;

@Mixin(BlockEntity.class)
@Extends(CapabilityProvider.class)
public abstract class BlockEntityInject implements IForgeBlockEntity, CapabilityProviderInjection, ICapabilityProviderImpl<BlockEntity>, BlockEntityExtensions, CapabilityInvalidationWorkaround {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$gatherCapabilities(BlockEntityType<?> type, BlockPos pos, BlockState blockState, CallbackInfo ci) {
        this.gatherCapabilities();
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void kilt$loadCapabilities(CompoundTag tag, CallbackInfo ci) {
        if (getCapabilities() != null && tag.contains("ForgeCaps"))
            deserializeCaps(tag.getCompound("ForgeCaps"));
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void kilt$saveCapabilities(CompoundTag tag, CallbackInfo ci) {
        if (getCapabilities() != null)
            tag.put("ForgeCaps", serializeCaps());
    }

    @Inject(method = "setRemoved", at = @At("TAIL"))
    private void kilt$updateAndInvalidateCapabilities(CallbackInfo ci) {
        this.invalidateCaps();
        requestModelDataUpdate();
    }

    @Override
    public void onChunkUnloaded() {
        this.invalidateCaps();
    }

    @Override
    public CompoundTag getPersistentData() {
        return this.getExtraCustomData();
    }

    @Override
    public void invalidateCaps() {
        this.kilt$invalidateCaps();
    }
}