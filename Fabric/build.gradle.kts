plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("io.freefair.lombok") version "8.14.2"
}

// Project Properties
val project_version: String by project
val project_group: String by project
val project_author: String by project

// Common Properties
val minecraft_version: String by project

// Fabric Properties
val fabric_main: String by project
val fabric_loader: String by project
val yarn_mappings: String by project
val fabric_version: String by project

// Build Properties
val lombok_version: String by project

val id = rootProject.name.lowercase()

version = project_version
group = project_group

loom {
    splitEnvironmentSourceSets()
    mods {
        create(id) {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${fabric_loader}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
    
    // Common module
    implementation(project(":Common"))
    // Lombok
    compileOnly("org.projectlombok:lombok:${lombok_version}")
    annotationProcessor("org.projectlombok:lombok:${lombok_version}")
}

tasks.jar {
    from(project(":Common").sourceSets.main.get().output)
}

tasks.remapJar {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("Fabric")
    
    doLast {
        copy {
            from(archiveFile.get().asFile)
            into(file("$rootDir/builds/fabric"))
        }
    }
}

tasks.processResources {
    val props = mapOf(
        "version" to version,
        "id" to id,
        "name" to rootProject.name,
        "author" to project_author,
        "fabric_main" to fabric_main
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}
