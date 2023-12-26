import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.3.2")
    }
}

plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("kapt") version "1.8.10"
    `java-library`
}

group = "com.androidx.aab"
version = "1.0"

repositories {
    mavenCentral()
    maven(url = "https://maven.google.com/")
}

dependencies {
    testImplementation(kotlin("test"))

    api("com.android.tools.build:bundletool:1.15.6")
    val aaptVersion = "8.0.2-9289358"
    api("com.android.tools.build:aapt2:$aaptVersion")
    api("com.android.tools.build:aapt2-proto:$aaptVersion")
    api("com.android.tools.build:apksig:8.2.0")
    api("com.google.protobuf:protobuf-java:3.25.1")
    api("com.google.guava:guava:33.0.0-jre")
    api("org.bouncycastle:bcprov-jdk15on:1.70")
    api("org.bouncycastle:bcpkix-jdk15on:1.70")
    api("info.picocli:picocli:4.7.5")
    kapt("info.picocli:picocli-codegen:4.7.5")
    // just resolve deps.
    api("javax.inject:javax.inject:1")
    api("com.google.dagger:dagger:2.50")
}

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
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

// TODO need debug the proguard rules.
tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    verbose()

    // Alternatively put your config in a separate file
    // configuration("config.pro")

    // Use the jar task output as a input jar. This will automatically add the necessary task dependency.
    injars(tasks.named("jar"))

    outjars("build/aabtool-$version.jar")

    val javaHome = System.getProperty("java.home")
    // Automatically handle the Java version of this build.
    if (System.getProperty("java.version").startsWith("1.")) {
        // Before Java 9, the runtime classes were packaged in a single jar file.
        libraryjars("$javaHome/lib/rt.jar")
    } else {
        // As of Java 9, the runtime classes are packaged in modular jmod files.
        libraryjars(
            // filters must be specified first, as a map
            mapOf(
                "jarfilter" to "!**.jar",
                "filter" to "!module-info.class"
            ),
            "$javaHome/jmods/java.base.jmod"
        )
    }

    dontobfuscate()
    dontwarn()
    dontoptimize()
    keepattributes("Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod")
    keepclasseswithmembernames(
        "class * {\n" +
            "    native <methods>;\n" +
            "}"
    )
    keepclassmembers(
        "enum * {\n" +
            "    public static **[] values();\n" +
            "    public static ** valueOf(java.lang.String);\n" +
            "}"
    )
    keep("class javax.inject.* { *; }")
    keep("class kotlin.Metadata { *; }")
    keep("class com.androidx.aab.* { *; }")
    printmapping("build/proguard-mapping.txt")
}
