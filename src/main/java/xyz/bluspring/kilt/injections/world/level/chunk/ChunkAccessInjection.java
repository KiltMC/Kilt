package xyz.bluspring.kilt.injections.world.level.chunk;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

@FabricInjectedInterface(ChunkAccess.class)
public interface ChunkAccessInjection {
    default void findBlocks(BiPredicate<BlockState, BlockPos> fineFilter, BiConsumer<BlockPos, BlockState> output) {
        throw KiltHelper.createMixinException(ChunkAccessInjection.class, "findBlocks");
    }

    default void findBlocks(Predicate<BlockState> predicate, BiPredicate<BlockState, BlockPos> fineFilter, BiConsumer<BlockPos, BlockState> output) {
        throw KiltHelper.createMixinException(ChunkAccessInjection.class, "findBlocks");
    }

    @Nullable
    default CompoundTag writeAttachmentsToNBT(HolderLookup.Provider provider) {
        throw KiltHelper.createMixinException(ChunkAccessInjection.class, "writeAttachmentsToNBT");
    }

    default void readAttachmentsFromNBT(HolderLookup.Provider provider, CompoundTag tag) {
        throw KiltHelper.createMixinException(ChunkAccessInjection.class, "readAttachmentsFromNBT");
    }

    default AttachmentHolder.AsField getAttachmentHolder() {
        throw KiltHelper.createMixinException(ChunkAccessInjection.class, "getAttachmentHolder");
    }

    @Nullable
    default Level getLevel() {
        return null;
    }
}
