// TRACKED HASH: 53c5190929b57765472764e578af300291448097
package xyz.bluspring.kilt.injects.world.level.chunk;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.level.chunk.ChunkAccessInjection;
import xyz.bluspring.kilt.workarounds.CommonLevelWorkaround;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

@Implements(@Interface(iface = CommonLevelWorkaround.class, prefix = "kilt$i$"))
@Mixin(ChunkAccess.class)
public abstract class ChunkAccessInject implements ChunkAccessInjection, BlockGetter, IAttachmentHolder {
    @Shadow @Final protected ChunkPos chunkPos;
    @Shadow public abstract void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> biConsumer);

    @Shadow
    public abstract void setUnsaved(boolean bl);

    @Unique private BiPredicate<BlockState, BlockPos> kilt$fineFilter;

    @Override
    public void findBlocks(BiPredicate<BlockState, BlockPos> fineFilter, BiConsumer<BlockPos, BlockState> output) {
        this.findBlocks(state -> fineFilter.test(state, BlockPos.ZERO), fineFilter, output);
    }

    @Override
    public void findBlocks(Predicate<BlockState> predicate, BiPredicate<BlockState, BlockPos> fineFilter, BiConsumer<BlockPos, BlockState> output) {
        this.kilt$fineFilter = fineFilter;
        this.findBlocks(predicate, output);
        this.kilt$fineFilter = null;
    }

    @WrapOperation(method = "findBlocks", at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"))
    private <T> boolean kilt$tryUseFineFilterForTest(Predicate<T> instance, T t, Operation<Boolean> original, @Local BlockPos.MutableBlockPos mutableBlockPos, @Local BlockPos pos, @Local(ordinal = 1) int y, @Local(ordinal = 2) int z, @Local(ordinal = 3) int x, @Local BlockState state) {
        if (this.kilt$fineFilter != null) {
            return this.kilt$fineFilter.test(state, mutableBlockPos.setWithOffset(pos, x, y, z));
        }

        return original.call(instance, t);
    }

    private final AttachmentHolder.AsField attachmentHolder = new AttachmentHolder.AsField(this);

    @Override
    public boolean hasAttachments() {
        return this.getAttachmentHolder().hasAttachments();
    }

    @Override
    public boolean hasData(AttachmentType<?> type) {
        return this.getAttachmentHolder().hasData(type);
    }

    @Override
    public <T> T getData(AttachmentType<T> type) {
        return this.getAttachmentHolder().getData(type);
    }

    @Override
    public @Nullable <T> T getExistingDataOrNull(AttachmentType<T> type) {
        return this.getAttachmentHolder().getExistingDataOrNull(type);
    }

    @Override
    public @Nullable <T> T setData(AttachmentType<T> type, T data) {
        this.setUnsaved(true);
        return this.getAttachmentHolder().setData(type, data);
    }

    @Override
    public @Nullable <T> T removeData(AttachmentType<T> type) {
        this.setUnsaved(true);
        return this.getAttachmentHolder().removeData(type);
    }

    @Override
    public @Nullable CompoundTag writeAttachmentsToNBT(HolderLookup.Provider provider) {
        return this.getAttachmentHolder().serializeAttachments(provider);
    }

    @Override
    public void readAttachmentsFromNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.getAttachmentHolder().deserializeAttachments(provider, tag);
    }

    @Override
    public AttachmentHolder.AsField getAttachmentHolder() {
        return this.attachmentHolder;
    }

    @Nullable
    @Override
    public Level getLevel() {
        return null;
    }

    @Intrinsic
    public Level kilt$i$getLevel() { // that's bullshit
        return this.getLevel();
    }
}
