plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("fr.brouillard.oss.gradle.jgitver")
    id("org.openapi.generator")
    id("maven-publish")
}

val coroutinesVersion = property("coroutines.version") as String
val serializationVersion = property("serialization.version") as String
val ktorVersion = property("ktor.version") as String

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")

                api("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-serialization:$ktorVersion")
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
    }
}

val generatedDir: File = layout.buildDirectory.dir("generated").get().asFile
val targetPackageName = "ru.playa.hc.intercom"

openApiGenerate {
    generatorName = "kotlin"
    library = "multiplatform"
    inputSpec = "$rootDir/specs/openapi.yml"
    outputDir = generatedDir.canonicalPath
    cleanupOutput = true
    packageName = targetPackageName
    configOptions = mapOf(
        "apiSuffix" to ""
    )
}

val generatedKotlinDir = generatedDir.resolve("src/commonMain/kotlin")
val targetKotlinDir = rootDir.resolve("src/commonMain/kotlin")
val targetReadmeDir = rootDir.resolve("readme")

val copyReadme by tasks.registering(Copy::class) {
    doFirst { targetReadmeDir.deleteRecursively() }
    from(generatedDir) {
        include("README.md", "docs/**/*")
    }
    into(targetReadmeDir)
}

val generateApi by tasks.registering(Copy::class) {
    dependsOn(tasks.openApiGenerate)
    doFirst { targetKotlinDir.deleteRecursively() }
    from(generatedKotlinDir)
    into(targetKotlinDir)
    finalizedBy(copyReadme)
}

repositories {
    maven("https://maven.pkg.github.com/onotoliy/opposite-treasure-api") {
        name = "GitHubPackages"
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

publishing {
    repositories {
        maven("https://maven.pkg.github.com/onotoliy/opposite-treasure-api") {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GH_USERNAME")
                password = System.getenv("GH_TOKEN")
            }
        }
    }
}