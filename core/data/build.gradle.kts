plugins {
    id("kiranaflow.android.library")
    id("kiranaflow.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.kiranaflow.core.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
}
