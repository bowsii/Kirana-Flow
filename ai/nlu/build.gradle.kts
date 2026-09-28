plugins {
    id("kiranaflow.android.library")
    id("kiranaflow.android.hilt")
}

android {
    namespace = "com.kiranaflow.ai.nlu"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":ai:runtime"))
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
