import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.ajoberstar.grgit.Grgit
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

plugins {
    kotlin("jvm")
    id ("fabric-loom") version "1.2-SNAPSHOT"
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
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft ("com.mojang:minecraft:${property("minecraft_version")}")
    mappings (loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.19.2:2022.11.27@zip")
    })
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
    implementation(include("com.github.llamalad7.mixinextras:mixinextras-fabric:0.2.0-beta.6")!!)
    annotationProcessor("com.github.llamalad7.mixinextras:mixinextras-fabric:0.2.0-beta.6")
    modImplementation(include("com.github.Chocohead:Fabric-ASM:v2.3")!!)
    modImplementation(include("io.github.tropheusj:serialization-hooks:0.3.26")!!)
    modImplementation(include("com.jamieswhiteshirt:reach-entity-attributes:2.3.0")!!)
    modImplementation("net.minecraftforge:forgeconfigapiport-fabric:${property("forgeconfigapiport_version")}")

    // required by Forge Config API Port
    implementation("com.electronwill.night-config:core:3.6.5")
    implementation("com.electronwill.night-config:toml:3.6.5")

    // Forge stuff
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
    modRuntimeOnly ("maven.modrinth:ferrite-core:5.0.3-fabric")
    modRuntimeOnly ("maven.modrinth:lazydfu:0.1.3")
    modRuntimeOnly ("maven.modrinth:sodium:mc1.19.2-0.4.4")
    modRuntimeOnly ("maven.modrinth:lithium:mc1.19.2-0.11.1")
    modRuntimeOnly ("maven.modrinth:starlight:1.1.1+1.19")
    modRuntimeOnly ("maven.modrinth:indium:1.0.9+mc1.19.2")

    runtimeOnly ("org.joml:joml:1.10.4")

    // apparently I need this for Nullable to exist
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

configurations.all {
    exclude("cpw.mods", "modlauncher")
}

val targetJavaVersion = "17"
val forgeCommitHash = property("forge_commit_hash")

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

    register("cloneForgeApi") {
        description = "Clones the Forge repository. It's best you use :getForgeApi."
        group = "kilt"

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
        group = "kilt"

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
        group = "kilt"
        doNotTrackState("The up-to-date patch track is entirely unreliable, and it's fast enough anyway to not have to bother about it.")

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

    register<Copy>("copyForgeResources") {
        group = "kilt"
        from("$buildDir/forge/src/main/resources")
        into("$projectDir/src/forge/resources")
    }

    processPatches {
        content = "$buildDir/forge/src/main/java"
        patches = "$projectDir/patches"
        destination = "$projectDir/src/forge/java"
        group = "kilt"

        finalizedBy("copyForgeResources")

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

    processResources {
        dependsOn("copyForgeResources")
        inputs.property("version", project.version)
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = targetJavaVersion
        dependsOn("processPatches")
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${archiveBaseName.get()}" }
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
        dependsOn("getForgeApi")
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

            val ferriteMixinPropsFile = File(configDir, "ferritecore.mixin.properties")

            if (!ferriteMixinPropsFile.exists())
                ferriteMixinPropsFile.createNewFile()

            ferriteMixinPropsFile.writeText("# Replace the blockstate neighbor table\n" +
                    "replaceNeighborLookup = true\n" +
                    "# Do not store the properties of a state explicitly and read themfrom the replace neighbor table instead. Requires replaceNeighborLookup to be enabled\n" +
                    "replacePropertyMap = true\n" +
                    "# Cache the predicate instances used in multipart models\n" +
                    "cacheMultipartPredicates = true\n" +
                    "# Avoid creation of new strings when creating ModelResourceLocations\n" +
                    "modelResourceLocations = false\n" + // this is the most important part
                    "# Do not create a new MultipartBakedModel instance for each block state using the same multipartmodel. Requires cacheMultipartPredicates to be enabled\n" +
                    "multipartDeduplication = true\n" +
                    "# Deduplicate cached data for blockstates, most importantly collision and render shapes\n" +
                    "blockstateCacheDeduplication = true\n" +
                    "# Deduplicate vertex data of baked quads in the basic model implementations\n" +
                    "bakedQuadDeduplication = true\n" +
                    "# Replace objects used to detect multi-threaded access to chunks by a much smaller field. This option is disabled by default due to very rare and very hard-to-reproduce crashes, use at your own risk!\n" +
                    "useSmallThreadingDetector = false\n" +
                    "# Use a slightly more compact, but also slightly slower representation for block states\n" +
                    "compactFastMap = false\n" +
                    "# Populate the neighbor table used by vanilla. Enabling this slightly increases memory usage, but can help with issues in the rare case where mods access it directly.\n" +
                    "populateNeighborTable = false\n")
        }
    }

    remapJar {
        val originalName = archiveBaseName.get()
        archiveBaseName.set("temp_$originalName")

        doLast {
            println("Modifying Kilt's refmap to add enum extension mixins' missing fields...")

            val file = archiveFile.get().asFile
            val jar = JarFile(file)

            // Adds $VALUES to the mixin refmaps manually.
            // I hope to god this is a temporary solution.

            val refmap = jar.getJarEntry("Kilt-refmap.json")
            val json = JsonParser.parseString(String(jar.getInputStream(refmap).readAllBytes())).asJsonObject

            val mappings = json.getAsJsonObject("mappings")
            val namedIntermediaryMappingData = json.getAsJsonObject("data").getAsJsonObject("named:intermediary")

            val pkg = "xyz/bluspring/kilt/mixin"
            val enumExtenderAccessors = mapOf(
                "ArmPoseAccessor" to "field_3404:[Lnet/minecraft/class_572\$class_573;",
                "BlockPathTypesAccessor" to "field_24:[Lnet/minecraft/class_7;",
                "EnchantmentCategoryAccessor" to "field_9077:[Lnet/minecraft/class_1886;",
                "GrassColorModifierAccessor" to "field_26432:[Lnet/minecraft/class_4763\$class_5486;",
                "MobCategoryAccessor" to "field_6301:[Lnet/minecraft/class_1311;",
                "RaiderTypeAccessor" to "field_16632:[Lnet/minecraft/class_3765\$class_3766;",
                "RarityAccessor" to "field_8905:[Lnet/minecraft/class_1814;",
                "RecipeBookCategoriesAccessor" to "field_1805:[Lnet/minecraft/class_314;",
                "RecipeBookTypeAccessor" to "field_25767:[Lnet/minecraft/class_5421;",
                "TransformTypeAccessor" to "field_4314:[Lnet/minecraft/class_809\$class_811",
                "TypeAccessor" to "field_6319:[Lnet/minecraft/class_1317\$class_1319"
            )

            enumExtenderAccessors.forEach { (className, fieldMapping) ->
                val fullName = "$pkg/$className"

                val mappingObj = if (!mappings.has(fullName))
                    JsonObject()
                else
                    mappings.getAsJsonObject(fullName)

                val dataObj = if (!namedIntermediaryMappingData.has(fullName))
                    JsonObject()
                else
                    namedIntermediaryMappingData.getAsJsonObject(fullName)

                mappingObj.addProperty("\$VALUES", fieldMapping)
                dataObj.addProperty("\$VALUES", fieldMapping)

                mappings.add(fullName, mappingObj)
                namedIntermediaryMappingData.add(fullName, dataObj)
            }

            json.add("mappings", mappings)

            val data = JsonObject()
            data.add("named:intermediary", namedIntermediaryMappingData)

            json.add("data", data)

            val outputFile = File(file.parentFile, file.name.replace("temp_", ""))
            val output = JarOutputStream(outputFile.outputStream())

            for (entry in jar.entries()) {
                if (entry.name == "Kilt-refmap.json") {
                    output.putNextEntry(entry)
                    output.write(GsonBuilder().setPrettyPrinting().create().toJson(json).toByteArray())
                    output.closeEntry()

                    continue
                }

                output.putNextEntry(entry)
                output.write(jar.getInputStream(entry).readAllBytes())
                output.closeEntry()
            }

            output.close()

            file.delete()
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