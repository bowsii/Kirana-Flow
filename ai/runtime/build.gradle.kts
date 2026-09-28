plugins {
    id("kiranaflow.android.library")
    id("kiranaflow.android.hilt")
}

android {
    namespace = "com.kiranaflow.ai.runtime"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.android)
}
