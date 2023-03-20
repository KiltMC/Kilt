import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.ajoberstar.grgit.Grgit

plugins {
    kotlin("jvm")
    id ("fabric-loom") version "1.1-SNAPSHOT"
    id ("maven-publish")
    id ("org.ajoberstar.grgit") version "5.0.0" apply false
    id ("com.brambolt.gradle.patching") version "2022.05.01-7057"
}

version = property("mod_version")!!
group = property("maven_group")!!
archivesName.set(property("archives_base_name")!! as String)

sourceSets {
    getByName("main") {
        java.srcDir("src/main/java")
        java.srcDir("src/main/kotlin")
        java.srcDir("src/forge/java")

        resources.srcDir("src/forge/resources")
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
        content {
            includeGroup("dev.cafeteria")
        }
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
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft ("com.mojang:minecraft:${property("minecraft_version")}")
    mappings (loom.officialMojangMappings())
    modImplementation ("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation ("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // Just because I like Kotlin more than Java
    modImplementation ("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    // Forge Reimplementations
    val portingLibs = listOf("accessors", "attributes", "base", "common", "constants", "entity", "extensions", "model_generators", "model_loader", "models", "networking", "obj_loader", "tags", "transfer", "lazy_registration", "fake_players")
    portingLibs.forEach { lib ->
        modImplementation(include("io.github.fabricators_of_create.Porting-Lib:$lib:${property("porting_lib_version")}")!!)
    }
    modImplementation ("dev.architectury:architectury-fabric:${property("architectury_version")}")
    implementation(include("com.github.LlamaLad7:MixinExtras:0.2.0-beta.1")!!)
    annotationProcessor ("com.github.LlamaLad7:MixinExtras:0.2.0-beta.1")
    modImplementation(include("com.github.Chocohead:Fabric-ASM:v2.3")!!)
    modImplementation(include("io.github.tropheusj:serialization-hooks:0.3.26")!!)
    modImplementation(include("com.jamieswhiteshirt:reach-entity-attributes:2.3.0")!!)
    modImplementation("net.minecraftforge:forgeconfigapiport-fabric:${property("forgeconfigapiport_version")}")

    // required by Forge Config API Port
    implementation("com.electronwill.night-config:core:3.6.5")
    implementation("com.electronwill.night-config:toml:3.6.5")

    // Forge stuff
    implementation(include("net.minecraftforge:eventbus:6.0.4")!!)
    implementation(include("net.minecraftforge:forgespi:6.0.2")!!)
    implementation(include("org.apache.maven:maven-artifact:3.8.5")!!)
    implementation(include("cpw.mods:securejarhandler:2.1.4")!!)
    implementation(include("net.jodah:typetools:0.8.3")!!)
    implementation(include("net.minecraftforge:unsafe:0.2.+")!!)
    implementation(include("org.jline:jline-reader:3.12.+")!!)
    implementation(include("net.minecrell:terminalconsoleappender:1.3.0")!!)

    // Remapping SRG to Intermediary
    implementation(include("net.minecraftforge:srgutils:0.4.13")!!)

    // Runtime mods for testing
    modRuntimeOnly ("com.terraformersmc:modmenu:4.1.0")
    //modRuntimeOnly ("curse.maven:ferritecore-fabric-459857:4117902") // 5.0.3
    modRuntimeOnly ("maven.modrinth:lazydfu:0.1.3")

    // apparently I need this for Nullable to exist
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

configurations.all {
    exclude("cpw.mods", "modlauncher")
}

val targetJavaVersion = "17"
val forgeCommitHash = property("forge_commit_hash")

tasks {
    register("cloneForgeApi") {
        description = "Clones the Forge repository. It's best you use :getForgeApi."

        doFirst {
            println("Cloning MinecraftForge repository to commit hash $forgeCommitHash..")
            val forgeSrcDir = File("$buildDir/forge")

            val grgit = if (!forgeSrcDir.exists())
                Grgit.clone(mutableMapOf<String, Any?>(
                    "uri" to "https://github.com/MinecraftForge/MinecraftForge.git",
                    "dir" to forgeSrcDir
                ))
            else
                Grgit.open(mutableMapOf<String, Any?>(
                    "dir" to forgeSrcDir
                ))

            grgit.fetch()
            grgit.checkout(mutableMapOf<String, Any?>(
                "branch" to forgeCommitHash
            ))

            println(grgit.describe())
        }
    }

    register("getForgeApi") {
        dependsOn("cloneForgeApi")
        finalizedBy("processPatches")
        description = "Clones the Forge repository, and places the API code into the 'forge' source set."

        doFirst {
            println("Copying Forge API-specific files into Kilt source dir...")

            val file = File("$projectDir/src/forge")
            if (file.exists()) {
                println("Found that Forge API already exists in a directory, replacing..")
                file.deleteRecursively()
            }
        }
    }

    createPatches {
        dependsOn("cloneForgeApi")
        content = "$buildDir/forge/src/main/java"
        modified = "$projectDir/src/forge/java"
        destination = "$projectDir/patches"

        doFirst {
            val patchesDir = File("$projectDir/patches")
            if (patchesDir.exists()) {
                println("Removing old patches before creating new patches...")
                patchesDir.deleteRecursively()
            }
        }

        doLast {
            println("Removing empty patches...")

            fun readDir(file: File): Boolean {
                val files = file.listFiles()!!

                if (files.isEmpty()) {
                    file.delete()
                    return true
                }

                var deletedCount = 0
                files.forEach {
                    if (it.isDirectory) {
                        if (readDir(it))
                            deletedCount++
                    } else {
                        if (it.readText().isBlank()) {
                            it.delete()
                            deletedCount++
                        }
                    }
                }

                if (deletedCount == files.size) {
                    file.delete()
                    return true
                }
                return false
            }

            readDir(File("$projectDir/patches"))
            readDir(File("$projectDir/patches")) // run again to clear empty dirs
        }
    }

    processPatches {
        content = "$buildDir/forge/src/main/java"
        patches = "$projectDir/patches"
        destination = "$projectDir/src/forge/java"

        doLast {
            println("Removing reimplemented Forge API sources...")

            val reimplemented = listOf(
                "net/minecraftforge/registries/DeferredRegister",
                "net/minecraftforge/registries/ForgeRegistries",
                "net/minecraftforge/registries/ForgeRegistry",
                "net/minecraftforge/registries/ForgeRegistryTag",
                "net/minecraftforge/registries/ForgeRegistryTagManager",
                "net/minecraftforge/registries/IForgeRegistry",
                "net/minecraftforge/registries/NewRegistryEvent",
                "net/minecraftforge/registries/RegisterEvent",
                "net/minecraftforge/registries/RegistryManager",
                "net/minecraftforge/registries/RegistryObject",
            )

            reimplemented.forEach {
                val file = File("$projectDir/src/forge/java/$it.java")
                file.delete()
            }
        }
    }

    jar {
        dependsOn("createPatches", "getForgeApi")
    }

    processResources {
        inputs.property("version", project.version)
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = targetJavaVersion
    }

    jar {
        from("LICENSE") {
            rename { "${it}_$archivesName" }
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