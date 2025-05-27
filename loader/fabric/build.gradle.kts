dependencies {
    // Cursed Fabric/Mixin stuff
    include(modApi("de.florianmichael:AsmFabricLoader:${property("asmfabricloader_version")}")!!)

    api(project(":loader"))
}