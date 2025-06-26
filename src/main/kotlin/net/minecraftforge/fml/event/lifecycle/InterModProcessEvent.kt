package net.minecraftforge.fml.event.lifecycle

import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModLoadingStage

class InterModProcessEvent(mod: ModContainer?, stage: ModLoadingStage?) : ParallelDispatchEvent(mod, stage) {
    constructor() : this(null, null)
}