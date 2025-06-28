package net.minecraftforge.fml.event.lifecycle

import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModLoadingStage

class FMLClientSetupEvent(mod: ModContainer?, stage: ModLoadingStage?) : ParallelDispatchEvent(mod, stage) {
    constructor() : this(null, null)

    private fun littleFunkyWorkaround() {
        throw IllegalStateException("You should not be able to access this!")
    }
}