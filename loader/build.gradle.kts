base {
    archivesName.set("Knit-Loader")
}

version = property("mod_version") as String

repositories {
    maven("https://maven.fabricmc.net")
}

dependencies {
    api("org.jetbrains:annotations:26.0.2")
    api("org.slf4j:slf4j-api:2.0.12")

    // Right off the bat, we'll get every Kotlin standard library we'd ever need from here.
    compileOnly("net.fabricmc:fabric-language-kotlin:${rootProject.property("fabric_kotlin_version")}")
}