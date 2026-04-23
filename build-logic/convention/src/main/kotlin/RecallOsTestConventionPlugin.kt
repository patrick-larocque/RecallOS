import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

class RecallOsTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

            dependencies.add("testImplementation", libs.library("junit4"))
            dependencies.add("testImplementation", libs.library("truth"))

            pluginManager.withPlugin("com.android.application") {
                configureAndroidTests(libs)
                extensions.configure(ApplicationExtension::class.java) {
                    testOptions {
                        unitTests.isIncludeAndroidResources = true
                    }
                }
            }

            pluginManager.withPlugin("com.android.library") {
                configureAndroidTests(libs)
                extensions.configure(LibraryExtension::class.java) {
                    testOptions {
                        unitTests.isIncludeAndroidResources = true
                    }
                }
            }
        }
    }

    private fun Project.configureAndroidTests(libs: VersionCatalog) {
        dependencies.add("testImplementation", libs.library("androidx-test-core"))
        dependencies.add("testImplementation", libs.library("robolectric"))
        dependencies.add("androidTestImplementation", libs.library("androidx-test-ext-junit"))
        dependencies.add("androidTestImplementation", libs.library("androidx-espresso-core"))
        dependencies.add("androidTestImplementation", libs.library("androidx-test-runner"))
    }
}

private fun VersionCatalog.library(alias: String) = findLibrary(alias).get()
