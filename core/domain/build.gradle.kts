plugins {
    id("kiranaflow.android.library")
    id("kiranaflow.android.hilt")
}

android {
    namespace = "com.kiranaflow.core.domain"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":ai:asr"))
    implementation(project(":ai:nlu"))
    implementation(project(":ai:audio"))
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
}
