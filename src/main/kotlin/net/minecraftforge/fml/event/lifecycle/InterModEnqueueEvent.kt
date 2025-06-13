package net.minecraftforge.fml.event.lifecycle

import net.minecraftforge.fml.ModLoadingStage
import xyz.bluspring.kilt.loader.mod.NeoForgeMod

class InterModEnqueueEvent(mod: NeoForgeMod?, stage: ModLoadingStage?) : ParallelDispatchEvent(mod, stage) {
    constructor() : this(null, null)
}