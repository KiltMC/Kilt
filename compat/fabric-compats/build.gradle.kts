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
    implementation("maven.modrinth:modernfix:${property("modernfix_version")}")
    implementation("software.bernie.geckolib:geckolib-fabric-${property("minecraft_version")}:${property("geckolib_version")}")
    compileOnly("maven.modrinth:modernkeybinding:${property("mkb_version")}") { // Modern Keybinding - The Maven repo is unstable, rely on Modrinth instead
        isTransitive = false
    }
    compileOnly("maven.modrinth:sophisticated-core-(unofficial-fabric-port):${property("sophisticatedcore_version")}")
    compileOnly("maven.modrinth:creativecore:${property("creativecore_version")}")
    compileOnly("de.siphalor:amecsapi-1.20:${property("amecsapi_version")}") {
        isTransitive = false
    }
    compileOnly("de.siphalor.amecs.amecs-key-modifiers:amecs-key-modifiers-mc1.21.1:${property("amecs_key_modifiers_version")}") {
        isTransitive = false
    }
    compileOnly("dev.emi:emi-fabric:${property("emi_version")}")
    compileOnly("mezz.jei:jei-${property("minecraft_version")}-fabric:${property("jei_version")}")
    compileOnly("me.shedaniel:RoughlyEnoughItems-fabric:${property("rei_version")}")
    compileOnly("me.shedaniel:RoughlyEnoughItems-api-neoforge:${property("rei_version")}")

    compileOnly("maven.modrinth:accessories:${property("accessories_version")}")
    compileOnly("maven.modrinth:snow-real-magic:${property("snowrealmagic_version")}")
    compileOnly("maven.modrinth:invmove:${property("invmove_version")}")

    compileOnly("maven.modrinth:pehkui:${property("pehkui_version")}")

    compileOnly("dev.architectury:architectury-fabric:${property("architectury_version")}")

    compileOnly("maven.modrinth:automodpack:${property("automodpack_version")}")

    compileOnly("maven.modrinth:veil:${property("veil_version")}-fabric,1.21.1")
    compileOnly("maven.modrinth:3KUWeVhG:${property("veil_version")}-neoforge,1.21.1") // funny workaround to get Veil Neo to be downloaded

    compileOnly("maven.modrinth:sable:${property("sable_version")}-fabric,1.21.1")
    compileOnly("maven.modrinth:T9PomCSv:${property("sable_version")}-neoforge,1.21.1") // same for Sable Neo

    compileOnly("maven.modrinth:resourceful-lib:${property("resourcefullib_version")}-fabric,1.21.1")

    compileOnly("cc.tweaked:cc-tweaked-26.1.2-common-api:${property("cc_tweaked_version")}")
    compileOnly("cc.tweaked:cc-tweaked-26.1.2-common:${property("cc_tweaked_version")}")
    modCompileOnly("cc.tweaked:cc-tweaked-26.1.2-fabric-api:${property("cc_tweaked_version")}")
    modCompileOnly("cc.tweaked:cc-tweaked-26.1.2-fabric:${property("cc_tweaked_version")}")
    compileOnly("cc.tweaked:cc-tweaked-26.1.2-core-api:${property("cc_tweaked_version")}")
    compileOnly("cc.tweaked:cc-tweaked-26.1.2-core:${property("cc_tweaked_version")}")
    compileOnly("cc.tweaked:cc-tweaked-26.1.2-forge-api:${property("cc_tweaked_version")}")
    compileOnlyLater("cc.tweaked:cc-tweaked-26.1.2-forge:${property("cc_tweaked_version")}")
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
