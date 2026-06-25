package xyz.bluspring.kilt.compat.create.ponder_bridged

import net.createmod.catnip.platform.services.ModFluidHelper
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.material.FluidState
import net.neoforged.neoforge.fluids.FluidStack

class WrappedModFluidHelper(val inner: ModFluidHelper<FluidVariant>) : ModFluidHelper<FluidStack> {
    private fun FluidVariant.toStack(): FluidStack = FluidStack(this.registryEntry, 1000)
    private fun FluidStack.toVariant(): FluidVariant = FluidVariant.of(this.fluid)

    @Environment(EnvType.CLIENT)
    override fun getColor(fluid: FluidStack, level: BlockAndTintGetter?, pos: BlockPos?): Int {
        return inner.getColor(fluid.toVariant(), level, pos);
    }

    override fun getLuminosity(fluid: FluidStack): Int {
        return inner.getLuminosity(fluid.toVariant())
    }

    @Environment(EnvType.CLIENT)
    override fun getStillTexture(fluid: FluidStack): TextureAtlasSprite? {
        return inner.getStillTexture(fluid.toVariant())
    }

    override fun isLighterThanAir(fluid: FluidStack): Boolean {
        return inner.isLighterThanAir(fluid.toVariant())
    }

    override fun toStack(state: FluidState): FluidStack {
        return inner.toStack(state).toStack()
    }
}
