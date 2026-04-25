plugins {
    id("recallos.android.library")
    id("recallos.compose")
    id("recallos.hilt")
    id("recallos.test")
}

android {
    namespace = "com.patricklarocque.recallos.feature.capture"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:files"))
    implementation(project(":core:model"))
    implementation(project(":core:navigation"))

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
}
