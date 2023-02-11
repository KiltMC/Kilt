package xyz.bluspring.kilt.resources

import net.fabricmc.fabric.api.resource.ModResourcePack
import net.fabricmc.fabric.impl.resource.loader.FabricModResourcePack
import net.minecraft.SharedConstants
import net.minecraft.server.packs.PackType
import xyz.bluspring.kilt.Kilt
import java.io.FileNotFoundException
import java.io.InputStream

class KiltResourcePack(type: PackType, packs: List<ModResourcePack>) : FabricModResourcePack(type, packs) {
    override fun getRootResource(string: String): InputStream? {
        if (string == "pack.mcmeta") {
            val description = "Forge mod resources"
            val pack = "{\"pack\":{\"pack_format\":${type.getVersion(SharedConstants.getCurrentVersion())},\"description\":\"$description\"}}"

            return pack.byteInputStream(Charsets.UTF_8)
        } else if (string == "pack.png") {
            return Kilt::class.java.getResourceAsStream("/assets/kilt/icon.png")
        }

        throw FileNotFoundException("\"$string\" not found in Fabric mod resource pack.")
    }

    override fun getName(): String {
        return "Kilt (Forge Mods)"
    }
}