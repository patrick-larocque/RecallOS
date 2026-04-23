plugins {
    id("recallos.android.library")
    id("recallos.hilt")
    id("recallos.test")
}

android {
    namespace = "com.patricklarocque.recallos.core.database"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    ksp(libs.androidx.room.compiler)

    testImplementation(libs.androidx.room.testing)
}
