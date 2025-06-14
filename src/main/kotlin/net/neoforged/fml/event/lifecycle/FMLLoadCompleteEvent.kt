package net.neoforged.fml.event.lifecycle

import net.minecraftforge.fml.ModLoadingStage
import xyz.bluspring.kilt.loader.mod.NeoForgeMod

class FMLLoadCompleteEvent(mod: NeoForgeMod?, stage: ModLoadingStage?) : ParallelDispatchEvent(mod, stage) {
    constructor() : this(null, null)

    private fun littleFunkyWorkaround() {
        throw IllegalStateException("You should not be able to access this!")
    }
}