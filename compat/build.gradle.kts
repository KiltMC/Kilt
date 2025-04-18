subprojects {
    apply(plugin = "fabric-loom")

    dependencies {
        implementation(project(rootProject.path, configuration = "namedElements"))
    }
}