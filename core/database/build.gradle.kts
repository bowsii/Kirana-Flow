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
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
}
