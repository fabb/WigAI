import org.gradle.api.tasks.bundling.Zip

plugins {
    // Apply the Java plugin to add support for Java
    java
}

group = "io.github.fabb"
version = "0.1.0"

repositories {
    mavenCentral()

    // Add the Bitwig Maven repository for the extension API
    maven {
        url = uri("https://maven.bitwig.com")
    }
}

dependencies {
    // Bitwig Extension API
    implementation("com.bitwig:extension-api:19")

    // MCP Java SDK
    // TODO: Replace with actual MCP Java SDK dependency once available
    // For initial development, this dependency might not be used directly yet
    // as the MCP server implementation will be stubbed in Story 1.2
    // implementation("io.modelcontextprotocol:mcp-java-sdk:1.0.0")

    // Use JUnit Jupiter for testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

java {
    // Configure Java 21 LTS
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    // Use JUnit Platform for unit tests
    useJUnitPlatform()
}

// Task to create the .bwextension file
tasks.register<Zip>("bwextension") {
    dependsOn("jar")

    archiveFileName.set("WigAI.bwextension")
    destinationDirectory.set(layout.buildDirectory.dir("extensions"))

    from(layout.buildDirectory.dir("libs")) {
        include("*.jar")
        into("lib")
    }

    from("src/main/resources") {
        include("META-INF/**")
    }
}

// Make the build task also create the .bwextension file
tasks.named("build") {
    dependsOn("bwextension")
}
