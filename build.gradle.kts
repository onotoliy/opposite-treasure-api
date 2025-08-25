import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "2.1.21" // kotlin_version
    kotlin("plugin.serialization") version "2.1.21" // kotlin_version
    id("org.openapi.generator") version "7.14.0"
    id("maven-publish")
}

group = "com.github.onotoliy.opposite.treasure"
version = project.hasProperty('newVersion') ? project.newVersion : '1.0.0-SNAPSHOT'

val kotlin_version = "2.1.21"
val coroutines_version = "1.10.2"
val serialization_version = "1.8.1"
val ktor_version = "3.1.3"

repositories {
    mavenCentral()
}

kotlin {
    jvm()

    js {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serialization_version")

                api("io.ktor:ktor-client-core:$ktor_version")
                api("io.ktor:ktor-client-serialization:$ktor_version")
                api("io.ktor:ktor-client-content-negotiation:$ktor_version")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

                api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-mock:$ktor_version")
            }
        }

        jvmMain {
            dependencies {
                implementation(kotlin("stdlib-jdk7"))
                implementation("io.ktor:ktor-client-cio-jvm:$ktor_version")
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        jsMain {
            dependencies {
                api("io.ktor:ktor-client-js:$ktor_version")
            }
        }
    }
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set(layout.projectDirectory.file("/specs/openapi.json").asFile.toURI().toString()) // Путь к вашей OpenAPI спецификации
    outputDir.set(layout.buildDirectory.dir("/generated").get().toString())
    packageName.set("com.github.onotoliy.opposite.treasure.client")
    apiPackage.set("com.github.onotoliy.opposite.treasure.api")
    modelPackage.set("com.github.onotoliy.opposite.treasure.model")
    invokerPackage.set("com.github.onotoliy.opposite.treasure.invoker")
    configOptions.putAll(
        mapOf(
            "dateLibrary" to "kotlinx-datetime",
            "library" to "multiplatform"
        )
    )
}

tasks {
    register("generateOpenApiClient") {
        dependsOn("openApiGenerate")
    }

    register("copyGeneratedSources") {
        doLast {
            val generatedSrcDir = layout.buildDirectory.dir("/generated/src")
            val targetDir = file("$projectDir/src")

            delete(targetDir)

            copy {
                from(generatedSrcDir)
                into(targetDir)
            }
        }
    }

    register("setVersion") {
        val newVersion = project.findProperty("newVersion") as String?
        doLast {
            if (newVersion == null) {
                throw GradleException("Please specify newVersion, e.g., -PnewVersion=1.2.3")
            }

            // Обновляем version в gradle.properties
            val propsFile = file("gradle.properties")
            val props = java.util.Properties()
            propsFile.inputStream().use { props.load(it) }

            props["VERSION_NAME"] = newVersion

            propsFile.outputStream().use { props.store(it, null) }

            println("Updated version to $newVersion in gradle.properties")
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            groupId = group.toString()
            artifactId = "opposite-treasure-service-api"
            version = version
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/onotoliy/opposite-treasure-api")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}