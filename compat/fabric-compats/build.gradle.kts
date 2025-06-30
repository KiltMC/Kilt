import net.fabricmc.loom.task.RemapJarTask

base {
    archivesName.set("Kilt-Fabric-Mod-Compats")
}

version = property("mod_version") as String

dependencies {
    modImplementation("maven.modrinth:modernfix:${property("modernfix_version")}")
    modImplementation("software.bernie.geckolib:geckolib-fabric-${property("minecraft_version")}:${property("geckolib_version")}")
    modCompileOnly("maven.modrinth:modernkeybinding:${property("mkb_version")}") { // Modern Keybinding - The Maven repo is unstable, rely on Modrinth instead
        isTransitive = false
    }
    modCompileOnly("maven.modrinth:sophisticated-core-(unofficial-fabric-port):${property("sophisticatedcore_version")}")
}

tasks {
    processResources {
        val properties = mutableMapOf(
            "version" to project.version,
            "loader_version" to project.property("loader_version"),
            "fabric_version" to project.property("fabric_version"),
            "minecraft_version" to project.property("minecraft_version"),
            "fabric_kotlin_version" to project.property("fabric_kotlin_version")
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