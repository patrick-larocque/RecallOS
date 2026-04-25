import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

class RecallOsComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.findByType(ApplicationExtension::class.java)?.buildFeatures?.compose = true
            extensions.findByType(LibraryExtension::class.java)?.buildFeatures?.compose = true

            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
            val composeBom = libs.library("androidx-compose-bom")

            dependencies.add("implementation", dependencies.platform(composeBom))
            dependencies.add("androidTestImplementation", dependencies.platform(composeBom))
            dependencies.add("implementation", libs.library("androidx-compose-ui"))
            dependencies.add("implementation", libs.library("androidx-compose-ui-tooling-preview"))
            dependencies.add("implementation", libs.library("androidx-material3"))
            dependencies.add("debugImplementation", libs.library("androidx-compose-ui-tooling"))
            dependencies.add("debugImplementation", libs.library("androidx-compose-ui-test-manifest"))
            dependencies.add("androidTestImplementation", libs.library("androidx-compose-ui-test-junit4"))
        }
    }
}

private fun VersionCatalog.library(alias: String) = findLibrary(alias).get()
