package xyz.bluspring.kilt.injections.client.particle;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface TerrainParticleInjection {
    default TerrainParticle updateSprite(BlockState state, BlockPos pos) {
        throw KiltHelper.createMixinException(TerrainParticleInjection.class, "updateSprite");
    }
}
