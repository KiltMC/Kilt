package xyz.bluspring.kilt.forgeinjects.client.particle;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.particle.TerrainParticleInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.BlockModelShaperInjection;

@Mixin(TerrainParticle.class)
public abstract class TerrainParticleInject extends TextureSheetParticle implements TerrainParticleInjection {
    protected TerrainParticleInject(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean kilt$checkAreParticlesTinted(BlockState instance, Block block, Operation<Boolean> original, @Local(argsOnly = true) ClientLevel level, @Local(argsOnly = true) BlockPos pos) {
        var properties = IClientBlockExtensions.of(instance);

        if (properties == IClientBlockExtensions.DEFAULT)
            return original.call(instance, block);

        return !properties.areBreakingParticlesTinted(instance, level, pos);
    }

    @Mixin(TerrainParticle.Provider.class)
    public abstract static class ProviderInject {
        @ModifyReturnValue(method = "createParticle(Lnet/minecraft/core/particles/BlockParticleOption;Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("RETURN"))
        private Particle kilt$handleUpdateSprite(Particle original, @Local(argsOnly = true) ClientLevel level, @Local(argsOnly = true) BlockParticleOption option) {
            if (original instanceof TerrainParticleInjection injection) {
                return injection.updateSprite(option.getState(), option.getSourcePos());
            }

            return original;
        }
    }

    @Override
    public Particle updateSprite(BlockState state, BlockPos pos) {
        if (pos != null) {
            this.setSprite(((BlockModelShaperInjection) Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()).getTexture(state, this.level, pos));
        }

        return this;
    }
}
