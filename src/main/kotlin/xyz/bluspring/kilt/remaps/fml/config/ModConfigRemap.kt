package xyz.bluspring.kilt.remaps.fml.config

import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.config.IConfigSpec
import net.minecraftforge.fml.config.ModConfig
import xyz.bluspring.kilt.loader.KiltModContainer

open class ModConfigRemap : ModConfig {
    constructor(type: Type, spec: IConfigSpec<*>, activeContainer: ModContainer) : super(type, spec, (activeContainer as KiltModContainer).fabricModContainer)
    constructor(type: Type, spec: IConfigSpec<*>, activeContainer: ModContainer, fileName: String) : super(type, spec, (activeContainer as KiltModContainer).fabricModContainer, fileName)
}