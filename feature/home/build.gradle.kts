plugins {
    id("recallos.android.library")
    id("recallos.compose")
    id("recallos.test")
}

android {
    namespace = "com.patricklarocque.recallos.feature.home"
}

dependencies {
    implementation(project(":core:designsystem"))
}
