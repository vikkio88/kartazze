import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "1.9.23"
    id("java-library")
//    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.28.0"
    signing
}

val ossrhToken: String by project
val ossrhUsername: String by project
val ossrhPassword: String by project


group = "io.github.vikkio88"
version = "0.0.1alpha"
val archiveName = "kartazze"

repositories {
    mavenCentral()
}
mavenPublishing {
    coordinates(
        groupId = group as String,
        artifactId = archiveName,
        version = version as String
    )
    pom {
        name.set("Kartazze")
        description.set("A little ORM/Query Builder Library")
        url.set("https://github.com/vikkio88/kartazze")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("vikkio88")
                name.set("Vincenzo Ciaccio")
                email.set("vincenzo.ciaccio@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/vikkio88/kartazze.git")
            developerConnection.set("scm:git:ssh://github.com/vikkio88/kartazze.git")
            url.set("https://github.com/vikkio88/kartazze")
        }
    }
    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}


//publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            from(components["java"])
//            artifactId = archiveName
//            pom {
//                name.set("Kartazze")
//                description.set("A little ORM/Query Builder Library")
//                url.set("https://github.com/vikkio88/kartazze")
//
//                licenses {
//                    license {
//                        name.set("The Apache License, Version 2.0")
//                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                    }
//                }
//
//                developers {
//                    developer {
//                        id.set("vikkio88")
//                        name.set("Vincenzo Ciaccio")
//                        email.set("vincenzo.ciaccio@gmail.com")
//                    }
//                }
//
//                scm {
//                    connection.set("scm:git:git://github.com/vikkio88/kartazze.git")
//                    developerConnection.set("scm:git:ssh://github.com/vikkio88/kartazze.git")
//                    url.set("https://github.com/vikkio88/kartazze")
//                }
//            }
//        }
//    }
//
//    repositories {
//        maven {
//            name = "ossrh"
//            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
//            credentials {
//                HttpHeaderAuthentication { "Bearer $ossrhToken" }
//                username = ossrhUsername
//                password = ossrhPassword
//            }
//        }
//
//
//        maven {
//            name = "ossrhSnapshots"
//            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
//            credentials {
//                HttpHeaderAuthentication { "Bearer $ossrhToken" }
//                username = ossrhUsername
//                password = ossrhPassword
//            }
//        }
//    }
//}


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
