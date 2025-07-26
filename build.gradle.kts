import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.daemon.common.toHexString
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import xyz.bluspring.kilt.gradle.AccessTransformerRemapper
import java.security.MessageDigest

plugins {
    kotlin("jvm")
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("maven-publish")
    id("org.ajoberstar.grgit") version "5.0.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.7.+"
}

version = "${createVersion()}${getVersionMetadata()}"
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

    getByName("test") {
        java.srcDirs("forge/src/test/java")
        resources.srcDir("forge/src/generated_test/resources")
        resources.srcDir("forge/src/test/resources")
    }
}

loom {
    accessWidenerPath.set(file("src/main/resources/kilt.accesswidener"))
    mixin {
        showMessageTypes.set(true)

        messages.set(mutableMapOf("ACCESSOR_TARGET_NOT_FOUND" to "disabled"))
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks {
        create("printConfigurations") {
            doLast {
                println("Project Name: ${project.name} configurations:")
                configurations.forEach { config ->
                    println("\t- ${config.name}")
                }
            }
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://mvn.devos.one/releases/") {
            name = "devOS Maven"
        }

        maven("https://mvn.devos.one/snapshots/") {
            name = "devOS Maven (Snapshots)"
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

        maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") {
            name = "GeckoLib"
        }

        maven("https://thedarkcolour.github.io/KotlinForForge/") {
            name = "Kotlin for Forge"
        }

        maven("https://maven.blamejared.com") {
            name = "BlameJared"
        }
    }

    // Avoid making the compats submodule use Loom, otherwise we break stuff
    if (project.name == "compat")
        return@allprojects

    // Prevent other Knit Loader modules from going through Fabric Loom.
    if (project.name == "loader" || (project.parent?.name == "loader"))
        return@allprojects

    apply(plugin = "fabric-loom")

    dependencies {
        // To change the versions see the gradle.properties file
        minecraft ("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
        mappings (loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${rootProject.property("parchment_version")}:${rootProject.property("parchment_release")}@zip")
        })
        modImplementation ("net.fabricmc:fabric-loader:${rootProject.property("loader_version")}")

        // Just because I like Kotlin more than Java
        modImplementation ("net.fabricmc:fabric-language-kotlin:${rootProject.property("fabric_kotlin_version")}")

        // TODO: remove this when 0.5 is mainlined into Fabric
        implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:${rootProject.property("mixinextras_version")}")!!)

        implementation("com.moulberry:mixinconstraints:${rootProject.property("mixinconstraints_version")}") {
            exclude("org.spongepowered", "mixin")
        }

        if (project.parent?.name != "loader") {
            // Fabric API. This is technically optional, but you probably want it anyway.
            modImplementation ("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_version")}")

            // Cursed Fabric/Mixin stuff
            implementation("com.github.FabricCompatibilityLayers:CursedMixinExtensions:${rootProject.property("cursedmixinextensions_version")}")
            modImplementation("com.github.Chocohead:Fabric-ASM:v${rootProject.property("fabric_asm_version")}")
            implementation(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${rootProject.property("mixin_squared_version")}")!!)
            modApi("de.florianmichael:AsmFabricLoader:${property("asmfabricloader_version")}")
        }
    }
}

dependencies {
    // Forge Reimplementations
    val portingLibs = listOf("accessors", "asm", "attributes", "base", "blocks", "brewing", "chunk_loading", "client_events", "common", "core", "data", "entity", "extensions", "fluids", "gametest", "gui_utils", "items", "lazy_registration", "level_events", "loot", "mixin_extensions", "model_builders", "model_generators", "model_loader", "model_materials", "models", "networking", "obj_loader", "recipe_book_categories", "registries", "tags", "tool_actions", "transfer", "utility")
    portingLibs.forEach { lib ->
        modApi(include("io.github.fabricators_of_create.Porting-Lib:$lib:${property("porting_lib_version")}")!!)
    }
    modApi("dev.architectury:architectury-fabric:${property("architectury_version")}")

    // JiJ'd into main JAR alone
    include("io.github.llamalad7:mixinextras-fabric:${property("mixinextras_version")}")
    include("com.github.FabricCompatibilityLayers:CursedMixinExtensions:${property("cursedmixinextensions_version")}")
    include("com.github.Chocohead:Fabric-ASM:v${property("fabric_asm_version")}")
    include("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${rootProject.property("mixin_squared_version")}")
    include("de.florianmichael:AsmFabricLoader:${property("asmfabricloader_version")}")
    include("com.moulberry:mixinconstraints:${rootProject.property("mixinconstraints_version")}") {
        exclude("org.spongepowered", "mixin")
    }

    //modImplementation(include("io.github.tropheusj:serialization-hooks:${property("serialization_hooks_version")}")!!)
    modImplementation(include("com.jamieswhiteshirt:reach-entity-attributes:${property("reach_entity_attributes_version")}")!!)
    modApi("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${property("forgeconfigapiport_version")}")

    // Forge stuff
    api(include("xyz.bluspring:eventbus:${property("eventbus_version")}") {
        exclude("cpw.mods", "modlauncher")
        exclude("net.minecraftforge", "modlauncher")
        exclude("net.minecraftforge", "securemodules")
    })
    implementation(include("net.minecraftforge:forgespi:${property("forgespi_version")}") {
        exclude("cpw.mods", "modlauncher")
        exclude("net.minecraftforge", "modlauncher")
        exclude("net.minecraftforge", "securemodules")
    })
    implementation(include("org.apache.maven:maven-artifact:3.8.5")!!)
    api(include("cpw.mods:securejarhandler:${property("securejarhandler_version")}")!!)
    implementation(include("net.jodah:typetools:0.8.3")!!)
    api(include("net.minecraftforge:unsafe:0.2.+")!!)
    implementation(include("net.minecraftforge:mergetool-api:1.0")!!)
    implementation(include("org.jline:jline-reader:3.12.+")!!)
    implementation(include("net.minecrell:terminalconsoleappender:1.3.0")!!)
    implementation(include("org.openjdk.nashorn:nashorn-core:${property("nashorn_version")}")!!) // for CoreMods

    // Remapping SRG to Intermediary
    implementation(include("xyz.bluspring:srgutils:${property("srgutils_version")}")!!)
    implementation(include("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")!!)

    modApi(include("teamreborn:energy:${property("teamreborn_energy_version")}")!!)

    // Use Kilt's fork of Sinytra Connector's fork of ForgeAutoRenamingTool
    implementation(include("xyz.bluspring:AutoRenamingTool:${property("forgerenamer_version")}")!!)
    implementation(include("net.fabricmc:tiny-remapper:${property("tiny_remapper_version")}")!!)

    fun modOptional(dependencyNotation: String, shouldRunInRuntime: Boolean, configuration: Action<ExternalModuleDependency> = Action {}) {
        if (shouldRunInRuntime) {
            modImplementation(dependencyNotation, configuration)
        } else {
            modCompileOnly(dependencyNotation, configuration)
        }
    }

    val runSodium = true

    // Runtime mods for testing
    modImplementation ("com.terraformersmc:modmenu:7.1.0") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modRuntimeOnly ("maven.modrinth:ferrite-core:6.0.1-fabric") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modOptional ("maven.modrinth:sodium:${property("sodium_version")}", runSodium)
    modRuntimeOnly ("maven.modrinth:lithium:mc1.20.1-0.11.2") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modOptional ("maven.modrinth:indium:${property("indium_version")}", runSodium)
    modOptional("maven.modrinth:iris:${property("iris_version")}", runSodium)

    // Need this for Iris
    modRuntimeOnly("io.github.douira:glsl-transformer:2.0.1")
    modRuntimeOnly("org.antlr:antlr4-runtime:4.13.1")
    modRuntimeOnly("org.anarres:jcpp:1.4.14")

    // apparently I need this for Nullable to exist
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    implementation(include("commons-codec:commons-codec:1.15")!!)

    // Compatibility layers
    listOf(
        "transfer-api-compat", "forge-compats", "create-compat",
        "curios-trinkets-compat", "fabric-compats"
    ).forEach { layer ->
        runtimeOnly(project(":compat:$layer", configuration = "namedElements"))
    }

    // Knit Loader
    api(project(":loader"))
    runtimeOnly(project(":loader:fabric", configuration = "namedElements"))
    include(project(":loader:fabric")) {
        isTransitive = false
    }
    /*include(project(":loader:quilt")) {
        isTransitive = false
    }*/

    // Test libraries
    testImplementation("net.fabricmc:fabric-loader-junit:${property("loader_version")}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.+")
    testImplementation("org.opentest4j:opentest4j:1.2.0") // needed for junit 5
    testImplementation("org.hamcrest:hamcrest-all:1.3") // needs advanced matching for list order
}

configurations.all {
    exclude("cpw.mods", "modlauncher")
}

val targetJavaVersion = "17"

kotlin {
    jvmToolchain(targetJavaVersion.toInt())
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }

    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

tasks {
    test {
        useJUnitPlatform()
    }

    register("countPatchProgress") {
        group = "kilt"
        description = "Counts the total of patches in Forge, and checks how many Kilt ForgeInjects there are, to check how much is remaining."

        doFirst {
            // Scan Forge patches dir
            fun readDir(file: File, list: MutableList<String> = mutableListOf(), root: File = file): List<String> {
                val files = file.listFiles()!!

                files.forEach {
                    if (it.isDirectory) {
                        readDir(it, list, root)
                    } else {
                        list.add(it.toRelativeString(root).replace("\\", "/").removePrefix("/"))
                    }
                }

                return list
            }

            val forgePatches = readDir(File("$projectDir/forge/patches/minecraft"))
            val forgePatchCount = forgePatches.size

            val kiltInjects = readDir(File("$projectDir/src/main/java/xyz/bluspring/kilt/forgeinjects"))
            val kiltInjectCount = kiltInjects.size

            forgePatches.filter {
                if (it.startsWith("com/mojang/"))
                    !kiltInjects.contains(it.removePrefix("com/mojang/").replace(".java.patch", "Inject.java"))
                else
                    !kiltInjects.contains(it.removePrefix("net/minecraft/").replace(".java.patch", "Inject.java"))
            }.forEach {
                println("[-] Missing patch: $it")
            }

            kiltInjects.filter {
                if (it.startsWith("blaze3d") || it.startsWith("math") || it.startsWith("realmsclient"))
                    !forgePatches.contains(("com/mojang/$it").replace("Inject.java", ".java.patch"))
                else
                    !forgePatches.contains(("net/minecraft/$it").replace("Inject.java", ".java.patch"))
            }.forEach {
                println("[!] Extra inject: $it")
            }

            println("Progress: $kiltInjectCount injects/$forgePatchCount patches (${String.format("%.2f", (kiltInjectCount.toDouble() / forgePatchCount.toDouble()) * 100.0)}%)")
        }
    }

    register("tagPatches") {
        group = "kilt"
        description = "Tags the Kilt ForgeInjects with their currently tracked patch hash to ensure they are all up to date."

        doFirst {
            fun readDir(file: File) {
                val files = file.listFiles()!!
                val md = MessageDigest.getInstance("SHA1")

                files.forEach {
                    if (it.isDirectory) {
                        readDir(it)
                    } else {
                        val startDir = it.absolutePath.replace("\\", "/").replaceBefore("forgeinjects/", "").replace("forgeinjects/", "")
                        val patchDir = if (startDir.startsWith("blaze3d") || startDir.startsWith("math")) "com/mojang/${startDir.replace("Inject.java", ".java.patch")}"
                            else "net/minecraft/${startDir.replace("Inject.java", ".java.patch")}"

                        val patchFile = File("$projectDir/forge/patches/minecraft/$patchDir")
                        if (!patchFile.exists()) {
                            println("!! WARNING !! Inject $startDir no longer has an associated patch file!")
                            return@forEach
                        }

                        val patchHash = md.digest(patchFile.readBytes()).toHexString()

                        val data = it.readLines().toMutableList()
                        if (!data[0].startsWith("// TRACKED HASH: ")) {
                            data.add(0, "// TRACKED HASH: $patchHash")
                            it.writeText(data.joinToString("\r\n"))
                        } else {
                            val oldHash = data[0].removePrefix("// TRACKED HASH: ")

                            if (oldHash != patchHash) {
                                println("Inject $startDir is outdated! (patch: $patchHash, inject: $oldHash) Updating hash...")
                                data[0] = "// TRACKED HASH: $patchHash"
                                it.writeText(data.joinToString("\r\n"))
                            }
                        }
                    }
                }
            }

            readDir(File("$projectDir/src/main/java/xyz/bluspring/kilt/forgeinjects"))
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

        for ((key, value) in properties) {
            inputs.property(key, value)
        }

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

    processTestResources {
        filesMatching("META-INF/mods.toml") {
            this.name = "forge.mods.toml"
        }
    }

    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion))
    }

    compileTestKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion))
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${archiveBaseName.get()}" }
        }
    }

    named<Jar>("sourcesJar") {
        duplicatesStrategy = DuplicatesStrategy.WARN
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

            loaderDepsFile.writeText(File("$projectDir/gradle/loader_dep_overrides.json").readText())
        }
    }

    register("transformerToWidener") {
        group = "kilt"

        doLast {
            val remapper = AccessTransformerRemapper()
            val transformerFile = File("$projectDir/forge/src/main/resources/META-INF/accesstransformer.cfg")
            val widenerFile = File("$projectDir/src/main/resources/kilt.accesswidener")

            remapper.convertTransformerToWidener(
                transformerFile.readText(),
                widenerFile,
                project.property("minecraft_version") as String,
                layout.buildDirectory.get().asFile
            )
        }
    }

    project.extensions.configure<ModPublishExtension>("publishMods") {
        file = project.tasks.named<RemapJarTask>("remapJar").get().archiveFile
        displayName = "Kilt v${project.version} (MC ${project.property("minecraft_version")})"
        version = project.version as String
        changelog = System.getenv("RELEASE_DESCRIPTION") ?: ""
        type = ReleaseType.ALPHA
        modLoaders.add("fabric")

        dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null
                || providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null

        modrinth {
            projectId = project.property("publishing.modrinth").toString()
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            minecraftVersions.add(project.property("minecraft_version") as String)

            requires("fabric-api", "fabric-language-kotlin", "architectury-api", "forge-config-api-port", "sodium", "indium")
            optional("modmenu")
            embeds("porting_lib")
            incompatible("async", "embeddium")
        }

        curseforge {
            type = ReleaseType.BETA // Because apparently CurseForge hides alpha builds.
            projectId = project.property("publishing.curseforge").toString()
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            minecraftVersions.add(project.property("minecraft_version") as String)

            requires("fabric-api", "fabric-language-kotlin", "architectury-api", "forge-config-api-port-fabric", "sodium", "indium")
            optional("modmenu")
            incompatible("embeddium")
        }
    }
}

fun isRelease(): Boolean {
    return rootProject.hasProperty("build.release")
}

// Versioning format:
// X.Y.Z
// X - Minecraft minor version increment
// Y - Minecraft patch version increment
// Z - Kilt version increment, based on the last tag
fun createVersion(): String {
    val mcVersionComps = (rootProject.property("minecraft_version") as String).split(".")
    val mcVersion = "${mcVersionComps[1]}.${mcVersionComps[2]}"

    val grgit = Grgit.open(mutableMapOf<String, Any?>(
        "dir" to File("$projectDir")
    ))

    var increment = 0
    var shouldBump = false
    for (tag in grgit.tag.list()) {
        val components = tag.name.removePrefix("v").split(".").map { it.toInt() }

        // Check if the tag is actually for this MC version
        if (components.getOrNull(0) != mcVersionComps.getOrNull(1)?.toIntOrNull() || components.getOrNull(1) != mcVersionComps.getOrNull(2)?.toIntOrNull())
            continue

        shouldBump = true

        // Select the highest increment available.
        if (components.getOrElse(2) { 0 } > increment) {
            increment = components[2]
        }
    }

    // Bump the version accordingly
    if (shouldBump && !isRelease())
        increment += 1

    val version = "$mcVersion.$increment"

    if (isRelease() && System.getenv("GITHUB_TAG") != null) {
        val tag = System.getenv("GITHUB_TAG")
        if (tag.lowercase().removePrefix("v") != version)
            throw IllegalStateException("The tag created doesn't match the increment version! Are you incrementing it correctly? ($version != ${tag.lowercase().removePrefix("v")})")
    }

    return version
}

fun getVersionMetadata(): String {
    if (isRelease())
        return ""

    val grgit = Grgit.open(mutableMapOf<String, Any?>(
        "dir" to File("$projectDir")
    ))
    val commitHash =
        System.getenv("GITHUB_SHA") ?: grgit.head().abbreviatedId

    return "+build.${commitHash.subSequence(0, 6)}${if (System.getenv("GITHUB_RUN_NUMBER") == null) "-local" else if (!isRelease()) "-nightly" else ""}"
}