package xyz.bluspring.kilt.injections.core.particles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;

public interface BlockParticleOptionInjection {
    BlockParticleOption setPos(BlockPos pos);
    BlockPos getPos();
}
