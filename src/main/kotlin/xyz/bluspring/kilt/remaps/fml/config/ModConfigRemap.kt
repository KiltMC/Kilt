package xyz.bluspring.kilt.remaps.fml.config

import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.config.IConfigSpec
import net.minecraftforge.fml.config.ModConfig

open class ModConfigRemap : ModConfig {
    constructor(type: Type, spec: IConfigSpec<*>, activeContainer: ModContainer) : super(type, spec, FabricLoader.getInstance().getModContainer(activeContainer.modId).orElseThrow())
    constructor(type: Type, spec: IConfigSpec<*>, activeContainer: ModContainer, fileName: String) : super(type, spec, FabricLoader.getInstance().getModContainer(activeContainer.modId).orElseThrow(), fileName)
}