package xyz.bluspring.kilt.forgeinjects.client.particle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TerrainParticle.class)
public abstract class TerrainParticleInject {
    @WrapOperation(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean kilt$checkAreParticlesTinted(BlockState instance, Block block, Operation<Boolean> original, @Local(argsOnly = true) ClientLevel level, @Local(argsOnly = true) BlockPos pos) {
        var properties = IClientBlockExtensions.of(instance);

        if (properties == IClientBlockExtensions.DEFAULT)
            return original.call(instance, block);

        return !properties.areBreakingParticlesTinted(instance, level, pos);
    }

    // TODO: implement updateSprite when not tired
}
