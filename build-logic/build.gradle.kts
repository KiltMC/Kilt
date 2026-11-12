plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.4.0"
    id("agency.highlysuspect.minivan") version libs.versions.minivan.get()
}

//val mc = minivan.minecraftBuilder(libs.versions.minecraft.get())
//    .build()
//    .minecraft
//
//project.dependencies.add("runtimeOnly", project.files(mc.minecraft))
//mc.dependencies.forEach { project.dependencies.add("runtimeOnly", it) }

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
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

    implementation("net.neoforged:neoforge:${libs.versions.neoforge.get()}:installer")
    implementation("net.fabricmc:sponge-mixin:0.17.5+mixin.0.8.7")
}
