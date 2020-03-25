plugins {
    kotlin("jvm") version "1.3.70"
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"
    id("fr.coppernic.versioning") version "3.1.2"
}

val ktor_version: String by project

group = "flecomte"
version = versioning.info.tag

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    repositories {
        maven {
            name = "ktor-voter"
            url = uri("https://maven.pkg.github.com/flecomte/ktor-voter")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("ktor-voter") {
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}
