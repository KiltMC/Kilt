package xyz.bluspring.kilt.injects.world.level.lighting;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineInject extends LightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
    @Shadow @Final private BlockPos.MutableBlockPos mutablePos;

    protected BlockLightEngineInject(LightChunkGetter chunkSource, BlockLightSectionStorage storage) {
        super(chunkSource, storage);
    }

    @Redirect(method = "getEmission", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int kilt$useForgeLightEmissionCheck(BlockState instance) {
        return instance.getLightEmission(chunkSource.getLevel(), mutablePos);
    }

    @Redirect(method = "method_51532", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int kilt$useForgeLightEmissionCheck(BlockState instance, @Local(argsOnly = true) BlockPos pos) {
        return instance.getLightEmission(chunkSource.getLevel(), pos);
    }
}
