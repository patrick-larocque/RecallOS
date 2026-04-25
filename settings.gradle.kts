pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RecallOS"

include(
    ":app",
    ":core:common",
    ":core:model",
    ":core:data",
    ":core:database",
    ":core:files",
    ":core:search",
    ":core:ai",
    ":core:routing",
    ":core:ingestion",
    ":core:designsystem",
    ":core:navigation",
    ":feature:home",
    ":feature:capture",
    ":feature:search",
    ":feature:memory",
    ":feature:spaces",
    ":feature:settings",
    ":sync",
)
