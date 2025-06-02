import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.bundling.Jar

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
    archiveFileName.set("wigai-all-${project.version}.jar")
    manifest.inheritFrom(tasks.named<org.gradle.api.tasks.bundling.Jar>("jar").get().manifest)
    mergeServiceFiles() // Merge META-INF/services for SPI
}

// Task to create the .bwextension file
tasks.register<Jar>("bwextension") {
    group = "build"
    description = "Creates the .bwextension file, a JAR with a proper manifest and all dependencies."

    dependsOn(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar"))

    archiveFileName.set("WigAI.bwextension")

    destinationDirectory.set(layout.buildDirectory.dir("extensions"))

    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version.toString(),
            "Implementation-Vendor" to project.group.toString(),
            "Created-By" to "Gradle ${gradle.gradleVersion}",
        )
    }

    // Include all content from the shadowJar.
    // This makes WigAI.bwextension effectively BE the shadow JAR,
    // but with the desired name and the manifest defined directly above.
    from(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").map { zipTree(it.archiveFile) })
}

// Make the build task also create the .bwextension file
tasks.named("build") {
    dependsOn("bwextension")
}

// Task to print just the version for CI/CD
tasks.register("printVersion") {
    group = "help"
    description = "Prints the project version"
    doLast {
        // Use System.out.print to avoid extra newlines
        print(project.version)
    }
    // Ensure this task doesn't show progress
    logging.captureStandardOutput(LogLevel.QUIET)
}
