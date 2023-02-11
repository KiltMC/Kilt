package xyz.bluspring.kilt.resources

import net.fabricmc.fabric.api.resource.ModResourcePack
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.pack.PackMetadataSection
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.Pack.PackConstructor
import net.minecraft.server.packs.repository.PackSource
import xyz.bluspring.kilt.Kilt
import java.util.function.Consumer
import java.util.function.Supplier

object KiltResourceLoader {
    private val RESOURCE_PACK_SOURCE = PackSource { Component.literal("Kilt (Forge Mod) - ").append(it) }

    @JvmStatic
    fun loadResources(type: PackType, consumer: Consumer<Pack>, factory: PackConstructor?) {
        val packs = mutableListOf<ModResourcePack>()
        Kilt.loader.mods.forEach { mod ->
            if (mod.modFile == null) // ignore Forge
                return@forEach

            val pack = ModNioResourcePack.create(
                ResourceLocation("kilt", mod.modInfo.mod.modId),
                mod.modInfo.mod.displayName,
                mod.container.fabricModContainer,
                null, type,
                ResourcePackActivationType.ALWAYS_ENABLED
            )

            if (pack != null)
                packs.add(pack)
        }

        if (packs.isNotEmpty()) {
            val profile = Pack.create("Kilt (Forge Mods)", true, {
                KiltResourcePack(type, packs)
            }, factory ?: PackFactory(type), Pack.Position.TOP, RESOURCE_PACK_SOURCE)

            if (profile != null)
                consumer.accept(profile)
        }
    }

    private class PackFactory(private val packType: PackType) : Pack.PackConstructor {
        override fun create(
            string: String,
            component: Component,
            bl: Boolean,
            supplier: Supplier<PackResources>,
            packMetadataSection: PackMetadataSection,
            position: Pack.Position,
            packSource: PackSource
        ): Pack {
            return Pack(string, component, bl, supplier, packMetadataSection, packType, position, packSource)
        }
    }
}