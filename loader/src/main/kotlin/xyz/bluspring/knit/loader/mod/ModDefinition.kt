package xyz.bluspring.knit.loader.mod

import java.nio.file.Path

data class ModDefinition(
    val id: String,
    val displayName: String,
    val description: String = "",
    val version: ModVersion,
    val dependencies: List<ModDependency>,
    val mixinConfigs: List<String>,
    val path: Path,
    val parentId: String? = null
)