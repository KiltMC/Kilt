// TRACKED HASH: 1082f297519f03c628f3f7e11a990fbd62a1bc0d
package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.BlockEntityExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.capabilities.BlockEntityCapabilityProviderImpl;

@Mixin(BlockEntity.class)
@Extends(CapabilityProvider.class)
public class BlockEntityInject implements IForgeBlockEntity, CapabilityProviderInjection, BlockEntityCapabilityProviderImpl, BlockEntityExtensions {
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
        return this.getCustomData();
    }
}