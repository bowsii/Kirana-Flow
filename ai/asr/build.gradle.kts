plugins {
    id("kiranaflow.android.library")
    id("kiranaflow.android.hilt")
}

android {
    namespace = "com.kiranaflow.ai.asr"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":ai:runtime"))
    implementation(libs.kotlinx.coroutines.android)
}
