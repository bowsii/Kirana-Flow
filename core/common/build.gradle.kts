plugins {
    id("kiranaflow.android.library")
}

android {
    namespace = "com.kiranaflow.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
