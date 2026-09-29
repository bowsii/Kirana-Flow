plugins {
    id("kiranaflow.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.kiranaflow.core.model"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.hilt.android)

    testImplementation(libs.junit)
}
