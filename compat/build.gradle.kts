subprojects {
    apply(plugin = "net.fabricmc.fabric-loom")

    dependencies {
        implementation(project(rootProject.path))

        api("net.neoforged:bus:${property("eventbus_version")}") {
            exclude("org.ow2.asm")
        }
    }
}
