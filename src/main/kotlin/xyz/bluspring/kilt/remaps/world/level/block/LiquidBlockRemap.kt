package xyz.bluspring.kilt.remaps.world.level.block

import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.material.FlowingFluid
import java.util.function.Supplier

open class LiquidBlockRemap(fluidSupplier: Supplier<out FlowingFluid>, properties: Properties) : LiquidBlock(fluidSupplier.get(), properties) {
    val fluid: FlowingFluid
        get() {
            return this.fluid
        }
}