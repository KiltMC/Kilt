package xyz.bluspring.kilt.injections.core.particles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(BlockParticleOption.class)
public interface BlockParticleOptionInjection {
    default BlockParticleOption setPos(BlockPos pos) {
        throw KiltHelper.createMixinException(BlockParticleOptionInjection.class, "setPos");
    }

    default BlockPos getPos() {
        throw KiltHelper.createMixinException(BlockParticleOptionInjection.class, "getPos");
    }
}
