import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    kotlin("jvm") version "1.8.0"
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

group = "net.azisaba"
version = "1.20.2+1.0.0a"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/public/") }
    maven { url = uri("https://jitpack.io/") }
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("com.github.TownyAdvanced:Towny:0.100.3.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
}

paperweight.reobfArtifactConfiguration.set(ReobfArtifactConfiguration.REOBF_PRODUCTION)

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
    jvmToolchain(17)
}
