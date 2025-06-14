package xyz.bluspring.kilt.loader

import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.fml.loading.moddiscovery.ModInfo
import net.neoforged.fml.ModContainer

object VanillaModContainer : ModContainer(ModInfo(null, FabricLoader.getInstance().getModContainer("minecraft").orElseThrow())) {
}