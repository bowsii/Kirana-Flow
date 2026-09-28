plugins {
    id("kiranaflow.android.library")
    id("kiranaflow.android.compose")
}

android {
    namespace = "com.kiranaflow.core.ui"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.material.icons.extended)
}
