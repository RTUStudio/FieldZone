plugins {
    java
    id("io.freefair.lombok") version "8.14.2"
    id("com.gradleup.shadow") version "9.0.2"
    id("xyz.jpenilla.run-paper") version "3.0.0-beta.1"
}

// Project Properties
val project_version: String by project
val project_group: String by project
val project_author: String by project

// Common Properties
val minecraft_version: String by project

// Bukkit Properties
val bukkit_main: String by project
val bukkit_version: String by project
val paper_plugin: String by project
val framework_version: String by project

// Build Properties
val lombok_version: String by project

version = project_version
group = project_group

// run-paper 설정 (테스트 서버 실행)
tasks.runServer {
    minecraftVersion(minecraft_version)
    downloadPlugins {
        url("https://ci.codemc.io/job/RTUStudio/job/RSFramework/lastSuccessfulBuild/artifact/builds/plugin/RSFramework-${framework_version}.jar")
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")

    maven {
        name = "SpigotMC"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "Sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    // RSFramework
    maven("https://repo.codemc.io/repository/rtustudio/")

    // PlaceholderAPI / ProtocolLib
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    // Common module
    implementation(project(":Common"))

    // Plugin API
    val plugin_api = if (paper_plugin.toBoolean()) {
        "io.papermc.paper:paper-api:${bukkit_version}-R0.1-SNAPSHOT"
    } else {
        "org.spigotmc:spigot-api:${bukkit_version}-R0.1-SNAPSHOT"
    }
    compileOnly(plugin_api)

    // RSFramework
    compileOnly("kr.rtustudio:framework-api:${framework_version}")
    compileOnly(fileTree("libs") { include("*.jar") })

    // Kyori Adventure
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.1")
    compileOnly("net.kyori:adventure-text-minimessage:4.16.0")

    // Google/Apache
    compileOnly("com.google.code.gson:gson:2.13.1")
    compileOnly("com.google.guava:guava:33.4.8-jre")
    compileOnly("org.apache.commons:commons-lang3:3.18.0")
    
    // FastUtil (성능 최적화)
    compileOnly("it.unimi.dsi:fastutil:8.5.15")

    // Dependency
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.dmulloy2:ProtocolLib:5.1.0")

    // Lombok
    compileOnly("org.projectlombok:lombok:${lombok_version}")
    annotationProcessor("org.projectlombok:lombok:${lombok_version}")
}

tasks.jar {
    finalizedBy("shadowJar")
}

tasks.shadowJar {
    archiveClassifier.set("Bukkit")
    archiveBaseName.set(rootProject.name)
    doLast {
        copy {
            from(archiveFile.get().asFile)
            into(file("$rootDir/builds/bukkit"))
            System.out.println(rootDir)
        }
    }
}

tasks.processResources {
    val split = minecraft_version.split(".")
    val plugin_api_version = split[0] + "." + split[1]
    val props = mapOf(
        "version" to version,
        "name" to rootProject.name,
        "main" to bukkit_main,
        "api_version" to plugin_api_version,
        "author" to project_author
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
