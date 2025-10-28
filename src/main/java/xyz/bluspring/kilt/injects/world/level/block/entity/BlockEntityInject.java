// TRACKED HASH: 1082f297519f03c628f3f7e11a990fbd62a1bc0d
package xyz.bluspring.kilt.injects.world.level.block.entity;

import io.github.fabricators_of_create.porting_lib.blocks.injects.BlockEntityInjection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.workarounds.CapabilityInvalidationWorkaround;

@Mixin(BlockEntity.class)
@Extends(AttachmentHolder.class)
public abstract class BlockEntityInject extends AttachmentHolder implements IBlockEntityExtension, IAttachmentHolder, BlockEntityInjection, CapabilityInvalidationWorkaround {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$gatherCapabilities(BlockEntityType<?> type, BlockPos pos, BlockState blockState, CallbackInfo ci) {
        this.gatherCapabilities();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void loadAttachments(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (tag.contains(AttachmentHolder.ATTACHMENTS_NBT_KEY, Tag.TAG_COMPOUND))
            ((AttachmentHolder) (Object) this).deserializeAttachments(registries, tag.getCompound(AttachmentHolder.ATTACHMENTS_NBT_KEY));
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void saveAttachments(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        CompoundTag attachmentsTag = ((AttachmentHolder) (Object) this).serializeAttachments(registries);
        if (attachmentsTag != null) tag.put(AttachmentHolder.ATTACHMENTS_NBT_KEY, attachmentsTag);
    }

    @Inject(method = "setRemoved", at = @At("TAIL"))
    private void kilt$updateAndInvalidateCapabilities(CallbackInfo ci) {
        this.invalidateCapabilities();
        requestModelDataUpdate();
    }

    @Inject(method = "clearRemoved", at = @At("TAIL"))
    private void invalidateCapabilities(CallbackInfo ci) {
        invalidateCapabilities();
    }

//    @Override TODO: I hate this and I don't feel like using my brain rn
//    public <T> @Nullable T setData(AttachmentType<T> type, T data) {
//        return super;
//    }

    @Override
    public void onChunkUnloaded() {
        this.invalidateCaps();
    }

    @Override
    public CompoundTag getPersistentData() {
        return this.getPortingLibPersistentData();
    }
}