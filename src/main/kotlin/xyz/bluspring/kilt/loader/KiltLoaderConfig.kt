package xyz.bluspring.kilt.loader

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.forgespi.language.IModInfo
import org.apache.maven.artifact.versioning.VersionRange
import xyz.bluspring.kilt.util.EnumUtils
import xyz.bluspring.knit.loader.mod.ModDependency.Type
import xyz.bluspring.knit.loader.mod.ModEnvironment
import java.nio.file.Path
import java.util.Optional

@JvmRecord
data class KiltLoaderConfig(
    /**
     * Represents the list of mod IDs that should not be getting resolved by Kilt.
     */
    val forceDisabledModIds: List<String> = emptyList(),

    /**
     * Dependency overrides, a Kilt alternative to https://wiki.fabricmc.net/tutorial:dependency_overrides
     */
    val dependencyOverrides: Map<String, Map<String, ModDependencyOverride>> = emptyMap()
) {
    companion object {
        val PATH: Path = FabricLoader.getInstance().configDir.resolve("kilt_overrides.json")
        val CODEC: Codec<KiltLoaderConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.listOf()
                    .optionalFieldOf("force_disabled_mods", emptyList())
                    .forGetter(KiltLoaderConfig::forceDisabledModIds),
                    Codec.unboundedMap(
                        Codec.STRING,
                        Codec.unboundedMap(
                            Codec.STRING,
                            ModDependencyOverride.CODEC
                        )
                    ).optionalFieldOf("dependency_overrides", emptyMap())
                    .forGetter(KiltLoaderConfig::dependencyOverrides)
            )
                .apply(instance, ::KiltLoaderConfig)
        }
    }

    data class ModDependencyOverride(
        val version: Optional<VersionRange> = Optional.empty(),
        val type: Optional<Type> = Optional.empty(),
        val side: Optional<ModEnvironment> = Optional.empty(),
        val ordering: Optional<IModInfo.Ordering> = Optional.empty()
    ) {
        companion object {
            val VERSION_CONSTRAINT_CODEC: Codec<VersionRange> = Codec.STRING.xmap(
                { VersionRange.createFromVersionSpec(it) },
                {it.toString()}
            )
            val CODEC: Codec<ModDependencyOverride> = RecordCodecBuilder.create { instance ->
                instance.group(
                    VERSION_CONSTRAINT_CODEC.optionalFieldOf("version").forGetter { it.version },
                    EnumUtils.createThrowingFallbackCodec<Type>().optionalFieldOf("type").forGetter { it.type },
                    EnumUtils.createThrowingFallbackCodec<ModEnvironment>().optionalFieldOf("side").forGetter { it.side },
                    EnumUtils.createThrowingFallbackCodec<IModInfo.Ordering>().optionalFieldOf("ordering").forGetter { it.ordering }
                ).apply(instance, ::ModDependencyOverride)
            }
        }
    }
}
