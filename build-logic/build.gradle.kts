import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    `kotlin-dsl`
}

group = "com.patricklarocque.recallos.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

sourceSets {
    named("main") {
        java.srcDir("convention/src/main/kotlin")
    }
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.findLibrary("android-gradle-plugin").get())
    implementation(libs.findLibrary("kotlin-gradle-plugin").get())
    implementation(libs.findLibrary("compose-gradle-plugin").get())
    implementation(libs.findLibrary("hilt-gradle-plugin").get())
    implementation(libs.findLibrary("ksp-gradle-plugin").get())
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "recallos.android.application"
            implementationClass = "RecallOsAndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "recallos.android.library"
            implementationClass = "RecallOsAndroidLibraryConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "recallos.kotlin.library"
            implementationClass = "RecallOsKotlinLibraryConventionPlugin"
        }
        register("compose") {
            id = "recallos.compose"
            implementationClass = "RecallOsComposeConventionPlugin"
        }
        register("hilt") {
            id = "recallos.hilt"
            implementationClass = "RecallOsHiltConventionPlugin"
        }
        register("test") {
            id = "recallos.test"
            implementationClass = "RecallOsTestConventionPlugin"
        }
    }
}
