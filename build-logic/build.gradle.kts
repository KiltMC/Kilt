plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.4.0"
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
    maven("https://maven.neoforged.net/releases")
    maven("https://mvn.devos.one/releases")
    maven("https://repo.sleeping.town/")
}

dependencies {
    implementation("org.ow2.asm:asm:9.9")
    implementation("org.ow2.asm:asm-tree:9.8")
    implementation("net.fabricmc:mapping-io:0.7.1")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("net.fabricmc:fabric-loom:${libs.versions.fabric.loom.get()}")
    implementation("net.neoforged:accesstransformers:11.0.1")
    implementation("agency.highlysuspect:minivan:${libs.versions.minivan.get()}")
}
