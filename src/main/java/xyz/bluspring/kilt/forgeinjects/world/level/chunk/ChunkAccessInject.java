// TRACKED HASH: 53c5190929b57765472764e578af300291448097
package xyz.bluspring.kilt.forgeinjects.world.level.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.ChunkAccessInjection;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessInject implements ChunkAccessInjection, BlockGetter {
    @Shadow public abstract LevelChunkSection getSection(int index);

    @Shadow @Final protected ChunkPos chunkPos;

    @Nullable
    @Override
    public LevelAccessor getWorldForge() {
        return null;
    }

    @Redirect(method = "findBlockLightSources", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;findBlocks(Ljava/util/function/Predicate;Ljava/util/function/BiConsumer;)V"))
    private void kilt$useForgeLightEmissionCheck(ChunkAccess instance, Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> output) {
        findBlocks((state, pos) -> {
            return state.getLightEmission((ChunkAccess) (Object) this, pos) != 0;
        }, output);
    }

    @Intrinsic
    public void findBlocks(BiPredicate<BlockState, BlockPos> predicate, BiConsumer<BlockPos, BlockState> output) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for(int i = this.getMinSection(); i < this.getMaxSection(); ++i) {
            LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndexFromSectionY(i));
            if (levelChunkSection.maybeHas((state) -> predicate.test(state, BlockPos.ZERO))) {
                BlockPos blockPos = SectionPos.of(this.chunkPos, i).origin();

                for(int j = 0; j < 16; ++j) {
                    for(int k = 0; k < 16; ++k) {
                        for(int l = 0; l < 16; ++l) {
                            BlockState blockState = levelChunkSection.getBlockState(l, j, k);
                            mutableBlockPos.setWithOffset(blockPos, l, j, k);
                            if (predicate.test(blockState, mutableBlockPos.immutable())) {
                                output.accept(mutableBlockPos, blockState);
                            }
                        }
                    }
                }
            }
        }
    }
}