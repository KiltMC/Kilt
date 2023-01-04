package net.minecraftforge.fml.event.lifecycle

import net.minecraftforge.fml.ModLoadingStage
import xyz.bluspring.kilt.loader.ForgeMod

class FMLConstructModEvent(mod: ForgeMod, stage: ModLoadingStage) : ParallelDispatchEvent(mod, stage) {
    private fun littleFunkyWorkaround() {
        throw IllegalStateException("You should not be able to access this!")
    }
}