import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
    id("org.jetbrains.compose") version "0.2.0-build132"
}

group = "alientech"
version = "1.0"

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.squareup.okhttp3:okhttp:3.8.1")
    implementation("net.jemzart:jsonkraken:2.0.0")
    implementation("com.google.api-client:google-api-client:1.30.10")
    implementation("io.ktor:ktor-websockets:1.5.1")
    implementation("com.fireflysource:firefly-kotlin-ext:4.9.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("com.google.code.gson:gson:2.8.6")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "CSSA Test Portal"
            version = "0.3"
            description = "Test portal application for CSSA"
            copyright = "© 2020 CSSA. All rights reserved."
            vendor = "CSSA"

            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
            }
        }
    }
}