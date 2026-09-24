import top.mrxiaom.gradle.LibraryHelper

plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "9.3.0"
    id ("com.github.gmazzo.buildconfig") version "5.6.7"
}
buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.8.1")
}
val base = LibraryHelper(project)

println("Group:   ${rootProject.group}")
println("Version: ${rootProject.version}")

val targetJavaVersion = 8
val pluginBaseModules = base.modules.run { listOf(library, message, paper, l10n, actions, gui, misc) }
val shadowGroup = "top.mrxiaom.sweet.rewards.libs"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly(base.depend.annotations)

    compileOnly("me.clip:placeholderapi:2.12.2")

    base.library(LibraryHelper.adventure("4.25.0"))
    base.library(base.depend.HikariCP)

    implementation("de.tr7zw:item-nbt-api:2.16.1")

    for (artifact in pluginBaseModules) {
        implementation(artifact)
    }
    implementation(base.resolver.lite)
}
buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.sweet.rewards")

    base.doResolveLibraries()

    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}

top.mrxiaom.gradle.LibraryHelper.initJava(project, base, targetJavaVersion, true)
top.mrxiaom.gradle.LibraryHelper.initPublishing(project)

tasks {
    shadowJar {
        configurations.add(project.configurations.runtimeClasspath.get())
        mapOf(
            "top.mrxiaom.pluginbase" to "base",
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
}
