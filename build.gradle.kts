plugins {
    java
    id("io.quarkus") version "3.30.4"
}

group = "gg.grounds"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/groundsgg/*")
        credentials {
            username = providers.gradleProperty("github.user").get()
            password = providers.gradleProperty("github.token").get()
        }
    }
}

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.30.8"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-grpc")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")
    implementation("gg.grounds:library-grpc-contracts-status:0.1.0")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-grpc")
    implementation("io.quarkus:quarkus-hibernate-reactive-panache")
    implementation("io.quarkus:quarkus-reactive-pg-client")
    implementation("io.quarkus:quarkus-smallrye-health")

    compileOnly("com.google.protobuf:protobuf-kotlin")

    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
}

sourceSets {
    main {
        java {
            srcDirs("build/classes/java/quarkus-generated-sources/grpc")
        }
    }
}

tasks.test {
    useJUnitPlatform()

    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.time=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
    )
    testLogging {
        // Show assertion diffs in test output
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.quarkusDev {
    jvmArgs = listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}