pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KiranaFlow"
include(":app")
include(":core:common")
include(":core:model")
include(":core:database")
include(":core:data")
include(":core:domain")
include(":ai:runtime")
include(":ai:audio")
include(":ai:asr")
include(":ai:nlu")
include(":core:ui")
include(":feature:billing")
include(":feature:stock")
include(":feature:pastbills")
