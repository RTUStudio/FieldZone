import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.plugins.JavaPluginExtension

val java_version: String by project

subprojects {
    extensions.findByType(JavaPluginExtension::class.java)?.apply {
        val jv = JavaVersion.toVersion(java_version.toInt())
        sourceCompatibility = jv
        targetCompatibility = jv
        if (JavaVersion.current() < jv) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(java_version.toInt()))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(java_version.toInt())
    }
}
