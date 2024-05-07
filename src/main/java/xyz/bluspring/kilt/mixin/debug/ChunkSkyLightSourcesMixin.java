package xyz.bluspring.kilt.mixin.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkSkyLightSources.class)
public class ChunkSkyLightSourcesMixin {
    @Inject(method = "findLowestSourceY", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/ChunkSkyLightSources;isEdgeOccluded(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void kilt$AAAAAAAAAAAAAAA(ChunkAccess chunk, int sectionIndex, int x, int z, CallbackInfoReturnable<Integer> cir, int i, BlockPos.MutableBlockPos mutableBlockPos, BlockPos.MutableBlockPos mutableBlockPos2, BlockState blockState, int j, LevelChunkSection levelChunkSection, int k, BlockState blockState2) {
        if (blockState2 == null) {
            System.out.println("for fuck's sake");
        }
    }
}
