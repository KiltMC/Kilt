package xyz.bluspring.kilt.injections.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface TerrainParticleInjection {
    default Particle updateSprite(BlockState state, BlockPos pos) {
        throw new IllegalStateException("TerrainParticleInjection.updateSprite");
    }
}
