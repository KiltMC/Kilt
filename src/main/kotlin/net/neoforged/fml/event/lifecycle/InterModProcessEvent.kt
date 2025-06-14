package net.neoforged.fml.event.lifecycle

import net.minecraftforge.fml.ModLoadingStage
import xyz.bluspring.kilt.loader.mod.NeoForgeMod

class InterModProcessEvent(mod: NeoForgeMod?, stage: ModLoadingStage?) : ParallelDispatchEvent(mod, stage) {
    constructor() : this(null, null)
}