package xyz.bluspring.kilt.loader

import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.loading.moddiscovery.ModInfo

object VanillaModContainer : ModContainer(ModInfo(null, FabricLoader.getInstance().getModContainer("minecraft").orElseThrow())) {
}