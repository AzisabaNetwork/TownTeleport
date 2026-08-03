plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.0"
}

group = "net.azisaba"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/public/") }
    maven { url = uri("https://jitpack.io/") }
    maven { url = uri("https://repo.azisaba.net/repository/maven-public/") }
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("net.azisaba:Townia:1.0-SNAPSHOT")
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
        archiveClassifier.set("")
        relocate("org.jetbrains.kotlin", "net.azisaba.townteleport.libs.org.jetbrains.kotlin")
        relocate("org.jetbrains.kotlinx", "net.azisaba.townteleport.libs.org.jetbrains.kotlinx")
        relocate("org.jetbrains.annotations", "net.azisaba.townteleport.libs.org.jetbrains.annotations")
        relocate("org.intellij.lang.annotations", "net.azisaba.townteleport.libs.org.intellij.lang.annotations")
        relocate("kotlin", "net.azisaba.townteleport.libs.kotlin")
    }
}

kotlin {
    jvmToolchain(21)
}
