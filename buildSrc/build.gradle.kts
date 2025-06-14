plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.1.21"
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
}

dependencies {
    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-tree:9.8")
    implementation("net.fabricmc:mapping-io:0.5.1")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    implementation("com.google.code.gson:gson:2.10.1")
}