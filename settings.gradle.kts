pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "FieldZone"

include(":Common")
include(":Bukkit")
include(":Fabric")
