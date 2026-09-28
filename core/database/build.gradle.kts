plugins {
    id("kiranaflow.android.library")
    id("kiranaflow.android.room")
    id("kiranaflow.android.hilt")
}

android {
    namespace = "com.kiranaflow.core.database"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.kotlinx.serialization.json)
    api("net.zetetic:sqlcipher-android:4.6.1")

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
}
