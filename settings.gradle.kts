pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // lokalne AAR-y
        flatDir { dirs("app/libs") }
    }
}
rootProject.name = "WolfGlyphBattery"
include(":app")
