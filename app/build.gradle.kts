plugins {
    id("recallos.android.application")
    id("recallos.compose")
    id("recallos.hilt")
    id("recallos.test")
}

android {
    namespace = "com.patricklarocque.recallos"

    defaultConfig {
        applicationId = "com.patricklarocque.recallos"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:files"))
    implementation(project(":core:ingestion"))
    implementation(project(":core:model"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:capture"))
    implementation(project(":feature:home"))
    implementation(project(":feature:search"))
    implementation(project(":feature:spaces"))
    implementation(project(":feature:settings"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hiltCompilerExtensions)
    implementation(libs.androidx.work.runtime.ktx)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
