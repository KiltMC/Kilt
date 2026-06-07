import net.fabricmc.loom.task.RemapJarTask
import xyz.bluspring.kilt.gradle.loom.KiltLoomPlugin

apply<KiltLoomPlugin>()

base {
    archivesName.set("Kilt-Fabric-Mod-Compats")
}

version = property("mod_version") as String

repositories {
    maven("https://maven.siphalor.de/")
}

dependencies {
    modImplementation("maven.modrinth:modernfix:${property("modernfix_version")}")
    modImplementation("software.bernie.geckolib:geckolib-fabric-${property("minecraft_version")}:${property("geckolib_version")}")
    modCompileOnly("maven.modrinth:modernkeybinding:${property("mkb_version")}") { // Modern Keybinding - The Maven repo is unstable, rely on Modrinth instead
        isTransitive = false
    }
    modCompileOnly("maven.modrinth:sophisticated-core-(unofficial-fabric-port):${property("sophisticatedcore_version")}")
    modCompileOnly("maven.modrinth:creativecore:${property("creativecore_version")}")
    modCompileOnly("de.siphalor:amecsapi-1.20:${property("amecsapi_version")}") {
        isTransitive = false
    }
    modCompileOnly("de.siphalor.amecs.amecs-key-modifiers:amecs-key-modifiers-mc1.21.1:${property("amecs_key_modifiers_version")}") {
        isTransitive = false
    }
    modCompileOnly("dev.emi:emi-fabric:${property("emi_version")}")
    modCompileOnly("mezz.jei:jei-${property("minecraft_version")}-fabric:${property("jei_version")}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-fabric:${property("rei_version")}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-neoforge:${property("rei_version")}")

    modCompileOnly("maven.modrinth:accessories:${property("accessories_version")}")
    modCompileOnly("maven.modrinth:snow-real-magic:${property("snowrealmagic_version")}")
    modCompileOnly("maven.modrinth:invmove:${property("invmove_version")}")

    modCompileOnly("maven.modrinth:pehkui:${property("pehkui_version")}")

    modCompileOnly("dev.architectury:architectury-fabric:${property("architectury_version")}")

    modCompileOnly("maven.modrinth:automodpack:${property("automodpack_version")}")

    modCompileOnly("maven.modrinth:veil:${property("veil_version")}-fabric,1.21.1")
    modCompileOnly("maven.modrinth:3KUWeVhG:${property("veil_version")}-neoforge,1.21.1") // funny workaround to get Veil Neo to be downloaded

    modCompileOnly("maven.modrinth:sable:${property("sable_version")}-fabric,1.21.1")
    modCompileOnly("maven.modrinth:T9PomCSv:${property("sable_version")}-neoforge,1.21.1") // same for Sable Neo

    modCompileOnly("maven.modrinth:resourceful-lib:${property("resourcefullib_version")}-fabric,1.21.1")
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
