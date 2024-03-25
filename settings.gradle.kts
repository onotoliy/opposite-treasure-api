rootProject.name = "opposite-treasure-api"

pluginManagement {
    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion

        val jgitverVersion = extra["jgitver.version"] as String
        id("fr.brouillard.oss.gradle.jgitver") version jgitverVersion

        val openapiGeneratorVersion = extra["openapi-generator.version"] as String
        id("org.openapi.generator") version openapiGeneratorVersion

        val foojayResolverVersion = extra["foojay-resolver.version"] as String
        id("org.gradle.toolchains.foojay-resolver-convention") version foojayResolverVersion
    }
}

