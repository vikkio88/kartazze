plugins {
    kotlin("jvm") version "1.9.23"
    id("java-library")
    id("maven-publish")
    signing
}

group = "io.github.vikkio88"
version = "0.0.1"

repositories {
    maven {
        name = "centralManualTesting"
        url = uri("https://central.sonatype.com/api/v1/publisher/deployments/download/")
        credentials {
            username = "someusername"
            password = "somepassword"
        }
        authentication {
            create<BasicAuthentication>("basic")
        }
    }
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "kartazze"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name = "Kartazze"
                description = "A little ORM/Query Builder Library"
                url = "https://github.com/vikkio88/kartazze"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "vikkio88"
                        name = "Vincenzo Ciaccio"
                        email = "vincenzo.ciaccio@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/vikkio88/kartazze.git"
                    developerConnection = "scm:git:ssh://github.com/vikkio88/kartazze.git"
                    url = "https://github.com/vikkio88/kartazze"
                }
            }
        }
    }
}

signing {
    setRequired {
        gradle.taskGraph.allTasks.any { it is PublishToMavenRepository }
    }
    sign(publishing.publications)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.xerial:sqlite-jdbc:3.46.0.0")
    // Add the SLF4J NOP binding, suppressed the warning
    testImplementation("org.slf4j:slf4j-nop:2.0.9")
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
