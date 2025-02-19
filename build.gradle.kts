import org.ajoberstar.grgit.Grgit

plugins {
    kotlin("jvm")
    id ("fabric-loom") version "1.9-SNAPSHOT"
    id ("maven-publish")
    id ("org.ajoberstar.grgit") version "5.0.0" apply false
}

version = "${property("mod_version")}+mc${property("minecraft_version")}${getVersionMetadata()}"
group = property("maven_group")!!
base {
    archivesName.set(property("archives_base_name")!! as String)
}

sourceSets {
    getByName("main") {
        java.srcDir("src/main/java")
        java.srcDir("src/main/kotlin")
        java.srcDir("forge/src/main/java")

        resources.srcDir("forge/src/generated/resources")
        resources.srcDir("forge/src/main/resources")
    }
}

loom {
    accessWidenerPath.set(file("src/main/resources/kilt.accesswidener"))
    mixin {
        showMessageTypes.set(true)

        messages.set(mutableMapOf("ACCESSOR_TARGET_NOT_FOUND" to "disabled"))
    }
}

repositories {
    maven("https://mvn.devos.one/snapshots/") {
        name = "DevOS One"
    }

    maven("https://jitpack.io/") {
        name = "JitPack"
    }

    maven("https://maven.cafeteria.dev/releases/") {
        name = "Cafeteria Dev"
    }

    maven("https://maven.jamieswhiteshirt.com/libs-release") {
        name = "JamiesWhiteShirt Dev"
        content {
            includeGroup("com.jamieswhiteshirt")
        }
    }

    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") {
        name = "Fuzs Mod Resources"
    }

    maven("https://maven.minecraftforge.net/") {
        name = "MinecraftForge Maven"
    }

    maven("https://maven.architectury.dev") {
        name = "Architectury"
    }

    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }

    mavenCentral()

    flatDir {
        dir("libs")
    }

    // Testing mod sources
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
    }

    maven("https://cursemaven.com") {
        name = "CurseMaven"
        content {
            includeGroup("curse.maven")
        }
    }

    maven("https://maven.terraformersmc.com/") {
        name = "TerraformersMC"
    }

    maven("https://maven.su5ed.dev/releases") {
        name = "Su5ed"
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft ("com.mojang:minecraft:${property("minecraft_version")}")
    mappings (loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_version")}:${property("parchment_release")}@zip")
    })
    modImplementation ("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation ("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // Just because I like Kotlin more than Java
    modImplementation ("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    // we require Indium due to us using Fabric Rendering API stuff.
    // let's tell the users that too.
    modImplementation(include("me.luligabi:NoIndium:${property("no_indium_version")}") {
        exclude("net.fabricmc", "fabric-loader")
    })

    // Forge Reimplementations
    val portingLibs = listOf("accessors", "attributes", "base", "common", "constants", "entity", "extensions", "model_generators", "model_loader", "models", "networking", "obj_loader", "tags", "transfer", "lazy_registration", "fake_players")
    portingLibs.forEach { lib ->
        modImplementation(include("io.github.fabricators_of_create.Porting-Lib:$lib:${property("porting_lib_version")}")!!)
    }
    modImplementation ("dev.architectury:architectury-fabric:${property("architectury_version")}")
    implementation(include(annotationProcessor("io.github.llamalad7:mixinextras-fabric:${property("mixinextras_version")}")!!)!!)
    implementation(include("com.github.thecatcore:CursedMixinExtensions:${property("cursedmixinextensions_version")}")!!)
    include(implementation(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${property("mixin_squared_version")}")!!)!!)
    modImplementation(include("com.github.Chocohead:Fabric-ASM:${property("fabric_asm_version")}")!!)
    modImplementation(include("io.github.tropheusj:serialization-hooks:${property("serialization_hooks_version")}")!!)
    modImplementation(include("com.jamieswhiteshirt:reach-entity-attributes:${property("reach_entity_attributes_version")}")!!)
    modImplementation("net.minecraftforge:forgeconfigapiport-fabric:${property("forgeconfigapiport_version")}")
    include(implementation("com.moulberry:mixinconstraints:${property("mixinconstraints_version")}") {
        exclude("org.spongepowered", "mixin")
    })
    include(modImplementation("de.florianmichael:AsmFabricLoader:${property("asmfabricloader_version")}")!!)

    // required by Forge Config API Port
    implementation("com.electronwill.night-config:core:3.6.5")
    implementation("com.electronwill.night-config:toml:3.6.5")

    // Forge stuff
    implementation(include("net.minecraftforge:eventbus:${property("eventbus_version")}")!!)
    implementation(include("net.minecraftforge:forgespi:${property("forgespi_version")}")!!)
    implementation(include("org.apache.maven:maven-artifact:3.8.5")!!)
    implementation(include("cpw.mods:securejarhandler:${property("securejarhandler_version")}")!!)
    implementation(include("net.jodah:typetools:0.8.3")!!)
    implementation(include("net.minecraftforge:unsafe:0.2.+")!!)
    implementation(include("org.jline:jline-reader:3.12.+")!!)
    implementation(include("net.minecrell:terminalconsoleappender:1.3.0")!!)
    implementation(include("org.openjdk.nashorn:nashorn-core:${property("nashorn_version")}")!!) // for CoreMods

    // Remapping SRG to Intermediary
    implementation(include("net.minecraftforge:srgutils:0.4.13")!!)

    modImplementation(include("teamreborn:energy:${property("teamreborn_energy_version")}")!!)

    // Use Kilt's fork of Sinytra Connector's fork of ForgeAutoRenamingTool
    implementation(include("xyz.bluspring:AutoRenamingTool:${property("forgerenamer_version")}")!!)

    // Runtime mods for testing
    modRuntimeOnly ("com.terraformersmc:modmenu:4.1.0") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modRuntimeOnly ("maven.modrinth:ferrite-core:5.0.3-fabric") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modRuntimeOnly ("maven.modrinth:lazydfu:0.1.3") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modImplementation ("maven.modrinth:sodium:mc1.19.2-0.4.4") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modRuntimeOnly ("maven.modrinth:lithium:mc1.19.2-0.11.1") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modRuntimeOnly ("maven.modrinth:starlight:1.1.1+1.19") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modRuntimeOnly ("maven.modrinth:indium:1.0.9+mc1.19.2") {
        exclude("net.fabricmc", "fabric-loader")
    }

    runtimeOnly ("org.joml:joml:1.10.4")

    // apparently I need this for Nullable to exist
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

configurations.all {
    exclude("cpw.mods", "modlauncher")
}

val targetJavaVersion = "17"

tasks {
    register("countPatchProgress") {
        group = "kilt"
        description = "Counts the total of patches in Forge, and checks how many Kilt ForgeInjects there are, to check how much is remaining."

        doFirst {
            // Scan Forge patches dir
            var count = 0

            fun readDir(file: File) {
                val files = file.listFiles()!!

                files.forEach {
                    if (it.isDirectory) {
                        readDir(it)
                    } else {
                        count++
                    }
                }
            }

            readDir(File("$buildDir/forge/patches"))

            val forgePatchCount = count
            count = 0

            readDir(File("$projectDir/src/main/java/xyz/bluspring/kilt/forgeinjects"))
            val kiltInjectCount = count

            println("Progress: $kiltInjectCount injects/$forgePatchCount patches (${String.format("%.2f", (kiltInjectCount.toDouble() / forgePatchCount.toDouble()) * 100.0)}%)")
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = targetJavaVersion
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${archiveBaseName.get()}" }
        }
    }

    processResources {
        val properties = mutableMapOf(
            "version" to project.version,
            "loader_version" to project.property("loader_version"),
            "fabric_version" to project.property("fabric_version"),
            "minecraft_version" to project.property("minecraft_version"),
            "fabric_kotlin_version" to project.property("fabric_kotlin_version"),
            "fabric_asm_version" to project.property("fabric_asm_version"),
            "forge_config_version" to project.property("forgeconfigapiport_version"),
            "architectury_version" to project.property("architectury_version"),
        )

        inputs.property("version", project.version)
        inputs.property("loader_version", project.property("loader_version"))
        inputs.property("fabric_version", project.property("fabric_version"))
        inputs.property("minecraft_version", project.property("minecraft_version"))
        inputs.property("fabric_kotlin_version", project.property("fabric_kotlin_version"))
        inputs.property("fabric_asm_version", project.property("fabric_asm_version"))
        inputs.property("forge_config_version", project.property("forgeconfigapiport_version"))
        inputs.property("architectury_version", project.property("architectury_version"))
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            // Use this instead of expand, as otherwise Gradle hard-errors when finding unknown $ names, and treats them as properties.
            this.filter {
                if (it.contains("\${")) {
                    var newString = it

                    for ((name, property) in properties) {
                        newString = newString.replace("\${$name}", property.toString())
                    }

                    return@filter newString
                }

                it
            }
        }

        // Rename Forge's mods.toml, so launchers like Prism don't end up detecting it over Kilt.
        filesMatching("META-INF/mods.toml") {
            this.name = "forge.mods.toml"
        }
    }

    // configure the maven publication
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }

        // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
        repositories {
            // Add repositories to publish to here.
            // Notice: This block does NOT have the same function as the block in the top level.
            // The repositories here will be used for publishing your artifact, not for
            // retrieving dependencies.
        }
    }

    register("setupDevEnvironment") {
        group = "kilt"

        doLast {
            val configDir = File("$projectDir/run/config")
            if (!configDir.exists())
                configDir.mkdirs()

            val loaderDepsFile = File(configDir, "fabric_loader_dependencies.json")

            if (!loaderDepsFile.exists())
                loaderDepsFile.createNewFile()

            loaderDepsFile.writeText("{\n" +
                    "  \"version\": 1,\n" +
                    "  \"overrides\": {\n" +
                    "    \"forgeconfigapiport\": {\n" +
                    "      \"-depends\": {\n" +
                    "        \"com_electronwill_night-config_core\": \"\",\n" +
                    "        \"com_electronwill_night-config_toml\": \"\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"kilt\": {\n" +
                    "      \"-depends\": {\n" +
                    "        \"com_electronwill_night-config_core\": \"\",\n" +
                    "        \"com_electronwill_night-config_toml\": \"\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}")
        }
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

fun getVersionMetadata(): String {
    val grgit = Grgit.open(mutableMapOf<String, Any?>(
        "dir" to File("$projectDir")
    ))
    val commitHash =
        System.getenv("GITHUB_SHA") ?: (grgit.head().abbreviatedId + if (!grgit.status().isClean) "-dirty" else "")

    return "+build.${commitHash.subSequence(0, 6)}"
}