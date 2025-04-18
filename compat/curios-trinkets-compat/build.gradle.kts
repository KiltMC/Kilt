base {
    archivesName.set("Kilt-Curios-Trinkets-Compats")
}

version = property("mod_version") as String

repositories {
    maven("https://maven.ladysnake.org/releases")
    maven("https://maven.theillusivec4.top/")
}

dependencies {
    modImplementation("dev.emi:trinkets:${property("trinkets_version")}")
    modCompileOnly("top.theillusivec4.curios:curios-forge:${property("curios_version")}:api")
    modCompileOnly("top.theillusivec4.curios:curios-forge:${property("curios_version")}")
}

tasks {
    processResources {
        val properties = mutableMapOf(
            "version" to project.version,
            "loader_version" to project.property("loader_version"),
            "fabric_version" to project.property("fabric_version"),
            "minecraft_version" to project.property("minecraft_version"),
            "fabric_kotlin_version" to project.property("fabric_kotlin_version"),
            "trinkets_version" to project.property("trinkets_version"),
            "curios_version" to project.property("curios_version")
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