package xyz.bluspring.kilt.injects.world.level.block.entity;

import java.util.Set;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import fr.catcore.cursedmixinextensions.annotations.ShadowSuper;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.world.level.block.entity.BlockEntityInjection;
import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.workarounds.AttachmentHolderWorkaround;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Extends(AttachmentHolder.class)
@Mixin(BlockEntity.class)
public abstract class BlockEntityInject implements BlockEntityInjection, IBlockEntityExtension, AttachmentHolderWorkaround, IAttachmentHolder {
    @Shadow public abstract BlockEntityType<?> getType();
    @Shadow public abstract void setChanged();
    @Shadow @Nullable protected Level level;
    @Shadow @Final protected BlockPos worldPosition;

    @Unique @Nullable private CompoundTag customPersistentData;
    @Unique @Nullable private Set<AttachmentType<?>> attachmentTypesToSync;

    @ShadowSuper("setData")
    public abstract <T> T kilt$super$setData(AttachmentType<T> type, T data);

    @ShadowSuper("removeData")
    public abstract @Nullable <T> T kilt$super$removeData(AttachmentType<T> type);

    @ModifyReceiver(method = "isValidBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntityType;isValid(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private BlockEntityType<?> kilt$tryUseCustomGetType(BlockEntityType<?> instance, BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(this.getClass(), BlockEntity.class, "getType", BlockEntityType.class)) {
            return this.getType();
        }

        return instance;
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void kilt$handleAdditionalLoad(ValueInput input, CallbackInfo ci) {
        input.read("NeoForgeData", CompoundTag.CODEC)
            .ifPresent(neoData -> this.customPersistentData = neoData);
        input.child(AttachmentHolder.ATTACHMENTS_NBT_KEY)
            .ifPresent(this::deserializeAttachments);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void kilt$handleAdditionalSave(ValueOutput output, CallbackInfo ci) {
        if (this.customPersistentData != null)
            output.store("NeoForgeData", CompoundTag.CODEC, this.customPersistentData.copy());

        //HolderLookup.Provider registries = this.level != null ? this.level.registryAccess() : RegistryAccess.EMPTY; // Kilt: this isn't used by NeoForge at all??? I'm commenting it out for now.
        var attachments = output.child(AttachmentHolder.ATTACHMENTS_NBT_KEY);
        this.serializeAttachments(attachments);

        if (attachments.isEmpty()) {
            output.discard(AttachmentHolder.ATTACHMENTS_NBT_KEY);
        }
    }

    @Inject(method = "setRemoved", at = @At("TAIL"))
    private void kilt$invalidateData(CallbackInfo ci) {
        this.invalidateCapabilities();
        this.requestModelDataUpdate();
    }

    @Inject(method = "clearRemoved", at = @At("TAIL"))
    private void kilt$invalidateCapabilitiesOnClearRemove(CallbackInfo ci) {
        this.invalidateCapabilities();
    }

    @Override
    public CompoundTag getPersistentData() {
        if (this.customPersistentData == null)
            this.customPersistentData = new CompoundTag();

        return this.customPersistentData;
    }

    @Override
    public <T> T setData(AttachmentType<T> type, T data) {
        this.setChanged();
        return this.kilt$super$setData(type, data);
    }

    @Override
    public @Nullable <T> T removeData(AttachmentType<T> type) {
        this.setChanged();
        return this.kilt$super$removeData(type);
    }

    @Override
    public void syncData(AttachmentType<?> type) {
        if (!(this.level instanceof ServerLevel serverLevel))
            return;

        if (this.attachmentTypesToSync == null)
            this.attachmentTypesToSync = new ReferenceOpenHashSet<>();

        this.attachmentTypesToSync.add(type);
        serverLevel.getChunkSource().blockChanged(this.worldPosition);
    }

    @Nullable
    @ApiStatus.Internal
    public Set<AttachmentType<?>> getAndClearAttachmentTypesToSync() {
        var ret = this.attachmentTypesToSync;
        this.attachmentTypesToSync = null;

        return ret;
    }
}
