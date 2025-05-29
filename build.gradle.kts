import org.gradle.api.tasks.bundling.Zip

plugins {
    // Apply the Java plugin to add support for Java
    java
    id("com.gradleup.shadow") version "8.3.0"
}

group = "io.github.fabb"
version = "0.2.0"

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
    // you can look up the documentation with tool context7
    // example implementation: https://modelcontextprotocol.io/sdk/java/mcp-server
    // version 0.10.0 uses MCP specification version 2024-11-05: https://modelcontextprotocol.io/specification/2024-11-05/basic/transports
    implementation(platform("io.modelcontextprotocol.sdk:mcp-bom:0.10.0"))
    implementation("io.modelcontextprotocol.sdk:mcp")
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    testImplementation("io.modelcontextprotocol.sdk:mcp-test")

    // Jetty 11 for embedded server and servlet support (EE9)
    implementation("org.eclipse.jetty:jetty-server:11.0.20")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.20")

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

// Configure the Shadow JAR (fat JAR with all dependencies)
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("wigai-all")
    archiveClassifier.set("") // No classifier, so it's just wigai-all-0.2.0.jar
    mergeServiceFiles() // Merge META-INF/services for SPI
}

// Task to create the .bwextension file
tasks.register<Zip>("bwextension") {
    dependsOn("shadowJar")
    archiveFileName.set("WigAI.bwextension")
    destinationDirectory.set(layout.buildDirectory.dir("extensions"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Avoid duplicate META-INF/services

    // Include all classes and dependencies from the fat JAR
    from(zipTree(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").get().archiveFile))

    // Include the services directory at the root level
    from("src/main/resources/META-INF") {
        include("services/**")
        into("META-INF")
    }
}

// Make the build task also create the .bwextension file
tasks.named("build") {
    dependsOn("bwextension")
}
