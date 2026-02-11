import net.fabricmc.loom.task.RemapJarTask
import xyz.bluspring.kilt.gradle.loom.KiltLoomPlugin

apply<KiltLoomPlugin>()

base {
    archivesName.set("Kilt-Forge-Compats")
}

loom {
    accessWidenerPath.set(file("src/main/resources/kilt-forge-compat.accesswidener"))
}

version = property("mod_version") as String

dependencies {
    modCompileOnly("maven.modrinth:immersiveengineering:${property("immersiveengineering_version")}")
    compileOnly("maven.modrinth:quark:${property("quark_version")}")
    compileOnly("org.violetmoon.zeta:Zeta:${property("zeta_version")}")
    modImplementation("maven.modrinth:sodium:${property("sodium_version")}")
    modCompileOnly("maven.modrinth:structure-gel-api:${property("structuregelapi_version")}")
    compileOnly("maven.modrinth:thirst-was-taken:${property("thirst_version")}")
    compileOnly("maven.modrinth:ldlib:${property("ldlib_version")}")
    compileOnly("maven.modrinth:littletiles:${property("littletiles_version")}")
    modCompileOnly("maven.modrinth:creativecore:${property("creativecore_version")}")
}

tasks {
    processResources {
        val properties = mutableMapOf(
            "version" to project.version,
            "loader_version" to project.property("loader_version"),
            "fabric_version" to project.property("fabric_version"),
            "minecraft_version" to project.property("minecraft_version"),
            "fabric_kotlin_version" to project.property("fabric_kotlin_version"),
            "sodium_version" to project.property("sodium_version")
        )

        for ((key, value) in properties) {
            inputs.property(key, value)
        }

        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }
}

// Add compat layer to nested JARs in base Kilt project.
rootProject.tasks.getByName<RemapJarTask>("remapJar").nestedJars.from(project.tasks.getByName("remapJar"))