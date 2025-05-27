plugins {
    id("org.quiltmc.loom") version "1.7.4"
}

repositories {
    // normally, this should be included by default with Quilt Loom, not sure why it's not though..
    maven("https://maven.quiltmc.org/repository/release/")
}

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${rootProject.property("parchment_version")}:${rootProject.property("parchment_release")}@zip")
    })

    modImplementation("org.quiltmc:quilt-loader:${property("quilt_loader_version")}")

    // TODO: use Quilt Kotlin Libraries when it's updated to Kotlin 2.1.21
    modImplementation ("net.fabricmc:fabric-language-kotlin:${rootProject.property("fabric_kotlin_version")}")

    // TODO: remove this when 0.5 is mainlined into Fabric
    include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:${rootProject.property("mixinextras_version")}")!!)!!)

    include(implementation("com.moulberry:mixinconstraints:${rootProject.property("mixinconstraints_version")}") {
        exclude("org.spongepowered", "mixin")
    })

    api(project(":loader"))
}