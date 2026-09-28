plugins {
    `kotlin-dsl`
}

group = "com.kiranaflow.buildlogic"

dependencies {
    compileOnly(libs.plugins.android.application.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${libs.versions.agp.get()}" })
    compileOnly(libs.plugins.android.library.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${libs.versions.agp.get()}" })
    compileOnly(libs.plugins.kotlin.android.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${libs.versions.kotlin.get()}" })
    compileOnly(libs.plugins.kotlin.compose.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${libs.versions.kotlin.get()}" })
    compileOnly(libs.plugins.ksp.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${libs.versions.ksp.get()}" })
    compileOnly(libs.plugins.hilt.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${libs.versions.hilt.get()}" })
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "kiranaflow.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidRoom") {
            id = "kiranaflow.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidHilt") {
            id = "kiranaflow.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidCompose") {
            id = "kiranaflow.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = "kiranaflow.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
