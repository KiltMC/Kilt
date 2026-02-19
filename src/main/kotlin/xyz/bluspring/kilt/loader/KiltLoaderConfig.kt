package xyz.bluspring.kilt.loader

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

@JvmRecord
data class KiltLoaderConfig(
    /**
     * Represents the list of mod IDs that should not be getting resolved by Kilt.
     */
    val forceDisabledModIds: List<String> = emptyList()
) {
    companion object {
        val PATH: Path = FabricLoader.getInstance().configDir.resolve("kilt_overrides.json")
        val CODEC: Codec<KiltLoaderConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.listOf()
                    .optionalFieldOf("force_disabled_mods", emptyList())
                    .forGetter(KiltLoaderConfig::forceDisabledModIds)
            )
                .apply(instance, ::KiltLoaderConfig)
        }
    }
}
