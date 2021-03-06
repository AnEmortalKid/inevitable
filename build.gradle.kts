import org.gradle.api.JavaVersion.VERSION_12
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion
val junitPlatformVersion = "1.4.1"
val junitJupiterVersion = "5.4.1"
val floggerVersion = "0.4"

plugins {
    kotlin("jvm") version "1.3.30"
    `java-library`
}

group = "eli.inevitable"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    implementation("com.google.guava:guava:27.1-jre")
    implementation("com.pinterest:ktlint:0.32.0")
    implementation("com.google.flogger:flogger:$floggerVersion")
    implementation("com.google.flogger:flogger-system-backend:$floggerVersion")

    testImplementation(kotlin("test", kotlinVersion))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.junit.platform:junit-platform-runner:$junitPlatformVersion")
    testImplementation("com.github.moove-it:fakeit:v0.7") {
        exclude(module = "appcompat-v7")
    }

    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testRuntime("org.junit.platform:junit-platform-engine:$junitPlatformVersion")
    testRuntime("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
}

java {
    sourceCompatibility = VERSION_12
    targetCompatibility = VERSION_12
}

configurations.create("ktlint")

tasks {
    withType<JavaExec> {
        group = "verification"
        description = "Check Kotlin code style"
        classpath = configurations.getByName("ktlint")
        main = "com.pinterest.ktlint.Main"
        args = listOf("src/**/*.kt")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "12"
    }

    withType<JavaExec> {
        group = "formatting"
        description = "Fix Kotlin code style deviations"
        classpath = configurations.getByName("ktlint")
        main = "com.pinterest.ktlint.Main"
        args = listOf("-F", "src/**/*.kt")
    }

    withType<Delete> {
        delete(rootProject.buildDir)
    }

    withType<Test> {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("FAILED", "SKIPPED", "PASSED")
            showStandardStreams = true
        }
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
        addTestListener(object: TestListener {
            override fun beforeSuite(descriptor: TestDescriptor?) {
                println("Initiating tests for: $descriptor")
            }

            override fun beforeTest(descriptor: TestDescriptor?) {
                println("Initiating test: $descriptor")
            }

            override fun afterTest(descriptor: TestDescriptor?, result: TestResult?) {
                println("Completed test: $descriptor")
            }

            override fun afterSuite(descriptor: TestDescriptor?, result: TestResult?) {
                println("Completed tests for: $descriptor")
                if(result != null) {
                    println("""Test results:    ${result.resultType}
                                |   Test Count: ${result.testCount}
                                |   Succeeded:  ${result.successfulTestCount}
                                |   Failed:     ${result.failedTestCount}
                                |   Skipped:    ${result.skippedTestCount}
                            """.trimMargin())
                }
            }
        })
    }
}
