subprojects {
    apply(plugin = "fabric-loom")

    dependencies {
        implementation(project(rootProject.path, configuration = "namedElements"))

        api("net.neoforged:bus:${property("eventbus_version")}") {
            exclude("org.ow2.asm")
        }
    }
}