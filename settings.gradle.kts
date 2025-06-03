plugins {
    id("com.mooltiverse.oss.nyx") version "3.1.3"
}

rootProject.name = "wigai"

// Nyx configuration for semantic versioning and releases
configure<com.mooltiverse.oss.nyx.gradle.NyxExtension> {
    preset.set("extended")

    services {
        register("github") {
            type.set("GITHUB")
            options.set(mapOf(
                "AUTHENTICATION_TOKEN" to "{{#environmentVariable}}GITHUB_TOKEN{{/environmentVariable}}",
                "REPOSITORY_OWNER" to "fabb",
                "REPOSITORY_NAME" to "WigAI",
            ))
        }
    }

    releaseTypes {
        publicationServices.set(listOf("github"))
    }

    // Release assets to upload
    releaseAssets {
        register("wigaiExtension") {
            fileName.set("WigAI.bwextension")
            description.set("WigAI Bitwig Extension for version {{version}}")
            type.set("application/java-archive")
            path.set("build/extensions/WigAI.bwextension")
        }
    }
}
