package xyz.bluspring.kilt.injects.client.particle;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.particle.TerrainParticleInjection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(TerrainParticle.class)
public abstract class TerrainParticleInject extends SingleQuadParticle implements TerrainParticleInjection {
    public TerrainParticleInject(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
    }

    @ModifyReturnValue(method = "createTerrainParticle", at = @At("RETURN"))
    private static TerrainParticle kilt$tryUpdateParticleSprite(TerrainParticle original, @Local(name = "state") BlockState state, @Local(argsOnly = true, name = "options") BlockParticleOption options) {
        if (original != null) {
            return original.updateSprite(state, options.getPos());
        }

        return null;
    }

    @Override
    public TerrainParticle updateSprite(BlockState state, BlockPos pos) {
        if (pos != null)
            this.setSprite(Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state, level, pos).sprite());

        return (TerrainParticle) (Object) this;
    }
}
