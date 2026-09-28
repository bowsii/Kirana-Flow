plugins {
    id("kiranaflow.android.library")
    id("kiranaflow.android.compose")
    id("kiranaflow.android.hilt")
}

android {
    namespace = "com.kiranaflow.feature.billing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":ai:asr"))
    implementation(project(":ai:nlu"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)
}
