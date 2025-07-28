subprojects {
    apply(plugin = "fabric-loom")

    dependencies {
        implementation(project(rootProject.path, configuration = "namedElements"))

        api("xyz.bluspring:eventbus:${rootProject.property("eventbus_version")}") {
            exclude("cpw.mods", "modlauncher")
            exclude("net.minecraftforge", "modlauncher")
            exclude("net.minecraftforge", "securemodules")
        }
        implementation("net.minecraftforge:forgespi:${rootProject.property("forgespi_version")}") {
            exclude("cpw.mods", "modlauncher")
            exclude("net.minecraftforge", "modlauncher")
            exclude("net.minecraftforge", "securemodules")
        }
    }
}