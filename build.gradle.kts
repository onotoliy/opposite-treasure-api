import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "1.9.22" // kotlin_version
    kotlin("plugin.serialization") version "1.9.22" // kotlin_version
    id("org.openapi.generator") version "6.0.1"
}

group = "org.openapitools"
version = "1.0.0"

val kotlin_version = "1.9.22"
val coroutines_version = "1.10.2"
val serialization_version = "1.8.1"
val ktor_version = "3.1.3"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
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

        iosMain {
            dependencies {
                api("io.ktor:ktor-client-ios:$ktor_version")
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
    generatorName.set("kotlin-multiplatform")
    inputSpec.set("$rootDir/specs/openapi.yml") // Путь к вашей OpenAPI спецификации
    outputDir.set("$buildDir/generated") // Папка для сгенерированного кода
    apiPackage.set("com.example.api")
    modelPackage.set("com.example.model")
    invokerPackage.set("com.example.invoker")
    configOptions.putAll(
        mapOf(
            "dateLibrary" to "kotlinx-datetime",
            "serializationLibrary" to "kotlinx_serialization"
            "library" to "multiplatform"
        )
    )
}

tasks {
    register("generateOpenApiClient") {
        dependsOn("openApiGenerate")
    }

    register("iosTest") {
        val device = project.findProperty("device")?.toString() ?: "iPhone 8"
        dependsOn("linkDebugTestIosX64")
        group = JavaBasePlugin.VERIFICATION_GROUP
        description = "Execute unit tests on ${device} simulator"
        doLast {
            val binary = kotlin.targets.getByName<KotlinNativeTarget>("iosX64").binaries.getTest("DEBUG")
            exec {
                commandLine("xcrun", "simctl", "spawn", device, binary.outputFile)
            }
        }
    }
    register("test") {
        dependsOn("allTests")
    }
}