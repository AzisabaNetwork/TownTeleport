plugins {
    kotlin("jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.azisaba"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/public/") }
    maven { url = uri("https://jitpack.io/") }
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.spigotmc:spigot:1.15.2-R0.1-SNAPSHOT")
    compileOnly("com.github.TownyAdvanced:Towny:0.96.1.11")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    testImplementation(kotlin("test"))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }

    processResources {
        // replace @version@
        filesMatching("*.yml") {
            filter(org.apache.tools.ant.filters.ReplaceTokens::class, mapOf("tokens" to mapOf("version" to project.version)))
        }
    }

    test {
        useJUnitPlatform()
    }

    shadowJar {
        relocate("org.jetbrains.kotlin", "net.azisaba.townteleport.libs.org.jetbrains.kotlin")
        relocate("org.jetbrains.kotlinx", "net.azisaba.townteleport.libs.org.jetbrains.kotlinx")
        relocate("org.jetbrains.annotations", "net.azisaba.townteleport.libs.org.jetbrains.annotations")
        relocate("org.intellij.lang.annotations", "net.azisaba.townteleport.libs.org.intellij.lang.annotations")
        relocate("kotlin", "net.azisaba.townteleport.libs.kotlin")
    }
}

kotlin {
    jvmToolchain(8)
}
