import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    kotlin("jvm") version "1.8.10"
    `java-library`
}

group = "com.androidx.aab"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://maven.google.com/")
}

dependencies {
    testImplementation(kotlin("test"))

    api("com.android.tools.build:bundletool:1.14.0")
    api("com.android.tools.build:aapt2:7.4.2-8841542")
    api("com.android.tools.build:aapt2-proto:7.4.2-8841542")
    api("com.android.tools.build:apksig:7.4.2")
    api("com.google.protobuf:protobuf-java:3.22.3")
    api("com.google.guava:guava:31.1-jre")
    api("org.bouncycastle:bcprov-jdk15on:1.70")
    api("org.bouncycastle:bcpkix-jdk15on:1.70")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    properties("javaVersion").let { version ->
        withType<JavaCompile> {
            sourceCompatibility = version
            targetCompatibility = version
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = version
            kotlinOptions.apiVersion = "1.8"
            kotlinOptions.languageVersion = "1.8"
            kotlinOptions.allWarningsAsErrors = false
        }
    }
    wrapper {
        gradleVersion = properties("gradleVersion")
    }
}

kotlin {
    jvmToolchain(properties("javaVersion").toInt())
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.androidx.aab.AABTool"
    }
    configurations["compileClasspath"].forEach { file: File ->
        project.logger.log(LogLevel.DEBUG, file.absolutePath)
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}