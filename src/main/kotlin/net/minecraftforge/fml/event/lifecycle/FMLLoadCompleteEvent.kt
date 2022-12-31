package net.minecraftforge.fml.event.lifecycle

import net.minecraftforge.fml.ModLoadingStage
import xyz.bluspring.kilt.loader.ForgeMod

class FMLLoadCompleteEvent(mod: ForgeMod, stage: ModLoadingStage) : ParallelDispatchEvent(mod, stage) {
}