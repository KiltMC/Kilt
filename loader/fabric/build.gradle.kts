dependencies {
    // Cursed Fabric/Mixin stuff
    include(modApi("de.florianmichael:AsmFabricLoader:${property("asmfabricloader_version")}")!!)
    include(implementation(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${rootProject.property("mixin_squared_version")}")!!)!!)

    api(project(":loader"))
}

tasks {
    processResources {
        val properties = mutableMapOf(
            "version" to project.version,
            "loader_version" to rootProject.property("loader_version"),
            "fabric_version" to rootProject.property("fabric_version"),
            "minecraft_version" to rootProject.property("minecraft_version"),
            "fabric_kotlin_version" to project.property("fabric_kotlin_version")
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
    }
}