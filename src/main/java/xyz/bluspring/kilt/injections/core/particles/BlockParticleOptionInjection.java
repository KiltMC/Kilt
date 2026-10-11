package xyz.bluspring.kilt.injections.core.particles;

import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;

@FabricInjectedInterface(BlockParticleOption.class)
public interface BlockParticleOptionInjection {
    default BlockParticleOption kilt$setPos(BlockPos pos) {
        throw KiltHelper.createMixinException(BlockParticleOptionInjection.class, "setPos");
    }

    default BlockPos getPos() {
        throw KiltHelper.createMixinException(BlockParticleOptionInjection.class, "getPos");
    }
}
