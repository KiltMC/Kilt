package xyz.bluspring.kilt.forgeinjects.core.particles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.core.particles.BlockParticleOptionInjection;

@Mixin(BlockParticleOption.class)
public class BlockParticleOptionInject implements BlockParticleOptionInjection {
    @Unique
    private BlockPos pos;

    @Override
    public BlockParticleOption setPos(BlockPos pos) {
        this.pos = pos;
        return (BlockParticleOption) (Object) this;
    }

    @Override
    public BlockPos getPos() {
        return this.pos;
    }
}
