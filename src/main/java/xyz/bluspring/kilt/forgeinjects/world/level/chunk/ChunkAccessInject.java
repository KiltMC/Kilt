// TRACKED HASH: 53c5190929b57765472764e578af300291448097
package xyz.bluspring.kilt.forgeinjects.world.level.chunk;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.ChunkAccessInjection;

@Mixin(ChunkAccess.class)
public class ChunkAccessInject implements ChunkAccessInjection {
    @Nullable
    @Override
    public LevelAccessor getWorldForge() {
        return null;
    }

    /*@Redirect(method = "findBlockLightSources", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;findBlocks(Ljava/util/function/Predicate;Ljava/util/function/BiConsumer;)V"))
    private void kilt$useForgeLightEmissionCheck(ChunkAccess instance, Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> output) {
        instance.findBlocks((state, pos) -> {
            return state.getLightEmission((ChunkAccess) (Object) this, pos) != 0;
        }, output);
    }*/
}