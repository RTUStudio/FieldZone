plugins {
    java
}

val project_version: String by project
val project_group: String by project

version = project_version
group = project_group

repositories {
    mavenCentral()
}

dependencies {
    // No external dependencies needed for Common
}
