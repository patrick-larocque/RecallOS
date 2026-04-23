plugins {
    id("recallos.android.library")
    id("recallos.compose")
    id("recallos.test")
}

android {
    namespace = "com.patricklarocque.recallos.feature.settings"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
}
