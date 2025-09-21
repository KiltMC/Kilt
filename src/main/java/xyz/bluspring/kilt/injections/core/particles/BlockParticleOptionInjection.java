package xyz.bluspring.kilt.injections.core.particles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(BlockParticleOption.class)
public interface BlockParticleOptionInjection {
    BlockParticleOption setPos(BlockPos pos);
    BlockPos getPos();
}
