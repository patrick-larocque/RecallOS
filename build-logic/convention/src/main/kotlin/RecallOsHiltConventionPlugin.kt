import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

class RecallOsHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.dagger.hilt.android")
            pluginManager.apply("com.google.devtools.ksp")

            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
            dependencies.add("implementation", libs.library("hilt-android"))
            dependencies.add("ksp", libs.library("hilt-compiler"))
        }
    }
}

private fun VersionCatalog.library(alias: String) = findLibrary(alias).get()
