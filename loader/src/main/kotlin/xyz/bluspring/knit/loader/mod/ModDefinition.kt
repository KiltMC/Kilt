package xyz.bluspring.knit.loader.mod

import java.nio.file.Path

/**
 * Provides basic identifying information for the mod. This is so native mod loaders such as Fabric and Quilt have information to identify the mods.
 */
data class ModDefinition(
    /**
     * The mod's file path, this will be used later to be injected into the classpath.
     * If the mod is going to be remapped, change this value.
     */
    var path: Path,

    /**
     * The mod's ID. Must be lowercase a-z, and may contain numbers, dashes or underscores.
     */
    val id: String,
    /**
     * The display name of the mod. This has a lot more freedom on what the name can be.
     */
    val displayName: String,
    /**
     * The description for the mod. May be blank.
     */
    val description: String = "",
    /**
     * The mod's version. This uses a custom class, as other loaders may use differing versions.
     */
    val version: ModVersion,
    /**
     * The authors behind the mod. This is intentionally made minimal compared to Fabric and Quilt's author data, as there's not much reason to handle that much info.
     */
    val authors: List<String> = emptyList(),
    /**
     * The mod's license. Should usually use SPDX license identifiers.
     */
    val license: String,

    /**
     * The mod's dependencies. The dependencies will then be handled by Knit later.
     */
    val dependencies: List<ModDependency> = listOf(),
    /**
     * The mod's mixin configs, which will then be loaded in by Knit during the mixin plugin phase.
     */
    val mixinConfigs: List<MixinConfig> = listOf(),
    /**
     * The mod's parent mod ID, this should typically be used by JiJ'd mods.
     */
    val parentId: String? = null,
    /**
     * The mod's icon path, this is recommended to be a square so mod menus can actually render this correctly, but Forge doesn't have that restriction, so.
     */
    val icon: String = "",
    /**
     * The environment that the mod loads in.
     */
    val environment: ModEnvironment = ModEnvironment.BOTH,

    /**
     * Specifies whether the mod definition is a built-in mod.
     */
    val isBuiltin: Boolean = false,

    /**
     * Additional arbitrary data that, while unused by Knit, is additional defining data for the mod loader to use, such as manifest information.
     */
    val additionalData: Map<String, Any?> = mapOf(),

    /**
     * Custom loader data to be used by the native mod loaders, such as Fabric and Quilt. This includes handling ModMenu metadata, which can be handled like this:
     *   ```kt
     *   loaderCustomData = mapOf(
     *      "modmenu" to mapOf(
     *          "badges" to listOf("library")
     *      )
     *   )
     *   ```
     */
    val loaderCustomData: Map<String, Any> = mapOf()
) {
    /**
     * An immutable version of the path that was originally provided to the mod definition. This can be useful for displaying a proper mod path to error logs.
     */
    val originalPath: Path = path

    data class MixinConfig(val config: String, val environment: ModEnvironment = ModEnvironment.BOTH)
}