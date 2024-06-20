plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.vikkio"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
    implementation("com.github.f4b6a3:ulid-creator:5.2.3")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    // Add the SLF4J NOP binding, suppressed the warning
    implementation("org.slf4j:slf4j-nop:2.0.9")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}