package xyz.bluspring.kilt.remaps.fml.config

import net.neoforged.fml.config.IConfigSpec
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.ModContainer
import xyz.bluspring.kilt.loader.KiltModContainer

/*open class ModConfigRemap : ModConfig {
    constructor(type: Type, spec: IConfigSpec<*>, activeContainer: ModContainer) : super(type, spec, (activeContainer as KiltModContainer).modId)
    constructor(type: Type, spec: IConfigSpec<*>, activeContainer: ModContainer, fileName: String) : super(type, spec, (activeContainer as KiltModContainer).modId, fileName)
}*?