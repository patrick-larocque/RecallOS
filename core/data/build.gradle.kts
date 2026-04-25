plugins {
    id("recallos.android.library")
    id("recallos.hilt")
    id("recallos.test")
}

android {
    namespace = "com.patricklarocque.recallos.core.data"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:files"))
    implementation(project(":core:model"))

    implementation(libs.androidx.room.ktx)

    testImplementation(libs.androidx.room.testing)
}
