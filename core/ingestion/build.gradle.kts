plugins {
    id("recallos.android.library")
    id("recallos.hilt")
    id("recallos.test")
}

android {
    namespace = "com.patricklarocque.recallos.core.ingestion"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:files"))
    implementation(project(":core:model"))

    implementation(libs.androidx.hilt.work)
    ksp(libs.hiltCompilerExtensions)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.google.mlkit.text.recognition)

    testImplementation(libs.androidx.work.testing)
}
