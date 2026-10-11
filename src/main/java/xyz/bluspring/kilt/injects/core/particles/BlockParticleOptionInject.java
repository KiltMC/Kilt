// TRACKED HASH: 2a097bd56b2bffdbb2ca713b3f2bf5c5f738f883
package xyz.bluspring.kilt.injects.core.particles;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.core.particles.BlockParticleOptionInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.particle.v1.FabricBlockParticleOption;
import net.fabricmc.fabric.impl.particle.BlockParticleOptionExtension;

@Mixin(BlockParticleOption.class)
public abstract class BlockParticleOptionInject implements BlockParticleOptionInjection, FabricBlockParticleOption {
    // Kilt: Implemented by Fabric API
    /*@Unique
    private @Nullable BlockPos pos;

    @ModifyReturnValue(method = "streamCodec", at = @At("RETURN"))
    private static StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> kilt$addConnectionAwareBlockPos(StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> original) {
        return StreamCodec.composite(
            original, Function.identity(),
            NeoForgeStreamCodecs.connectionAware(
                ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
                NeoForgeStreamCodecs.uncheckedUnit(Optional.empty())
            ), option -> Optional.ofNullable(option.getPos()),
            (value, pos) -> {
                if (pos.isPresent()) {
                    value.kilt$setPos(pos.orElseThrow());
                }

                return value;
            }
        );
    }

    @Override
    public BlockParticleOption kilt$setPos(BlockPos pos) {
        ((BlockParticleOptionExtension) this).fabric_setBlockPos(pos);
        return (BlockParticleOption) (Object) this;
    }
    */

    public BlockParticleOptionInject(ParticleType<BlockParticleOption> type, BlockState state) {}

    @CreateInitializer
    public BlockParticleOptionInject(ParticleType<BlockParticleOption> type, BlockState state, @Nullable BlockPos pos) {
        this(type, state);
        ((BlockParticleOptionExtension) this).fabric_setBlockPos(pos);
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }
}
