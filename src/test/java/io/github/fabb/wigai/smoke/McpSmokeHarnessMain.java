package io.github.fabb.wigai.smoke;

import io.github.fabb.wigai.common.AppConstants;

import java.time.Duration;

/**
 * CLI entrypoint for the MCP smoke harness.
 *
 * Usage:
 *   java McpSmokeHarnessMain [--host HOST] [--port PORT] [--endpoint PATH]
 *
 * Environment variables:
 *   WIGAI_SMOKE_TEST_MUTATIONS=true  - Enable mutation tests (off by default)
 *
 * Exit codes:
 *   0 = all checks passed
 *   1 = one or more checks failed or connection error
 */
public final class McpSmokeHarnessMain {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = AppConstants.DEFAULT_MCP_PORT;
    private static final String DEFAULT_ENDPOINT = "/mcp";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    public static void main(String[] args) {
        McpSmokeHarnessArgs harnessArgs = parseArgs(args);
        // Note: URL is printed by the harness itself in its formatted output

        McpClient client = new HttpMcpClient(harnessArgs.resolvedUrl(), DEFAULT_TIMEOUT);
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(harnessArgs, client, System.out, System.err);
        System.exit(exitCode);
    }

    static McpSmokeHarnessArgs parseArgs(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String endpoint = DEFAULT_ENDPOINT;
        boolean mutations = isMutationsEnabled();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--host", "-h" -> {
                    if (i + 1 < args.length) {
                        host = args[++i];
                    }
                }
                case "--port", "-p" -> {
                    if (i + 1 < args.length) {
                        String portArg = args[++i];
                        try {
                            port = Integer.parseInt(portArg);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid port: " + portArg + " (expected numeric)");
                            port = DEFAULT_PORT;
                        }
                    }
                }
                case "--endpoint", "-e" -> {
                    if (i + 1 < args.length) {
                        endpoint = args[++i];
                    }
                }
                // NOTE: --mutations/-m CLI flags intentionally removed per AC4
                // Mutations MUST be gated by WIGAI_SMOKE_TEST_MUTATIONS env var ONLY
                case "--help" -> {
                    printHelp();
                    System.exit(0);
                }
            }
        }

        return new McpSmokeHarnessArgs(host, port, endpoint, mutations);
    }

    private static boolean isMutationsEnabled() {
        String envValue = System.getenv("WIGAI_SMOKE_TEST_MUTATIONS");
        return "true".equalsIgnoreCase(envValue);
    }

    private static void printHelp() {
        System.out.println("""
            MCP Smoke Test Harness

            Usage: java McpSmokeHarnessMain [OPTIONS]

            Options:
              --host, -h HOST       MCP server host (default: localhost)
              --port, -p PORT       MCP server port (default: 61169)
              --endpoint, -e PATH   MCP endpoint path (default: /mcp)
              --help                Show this help message

            Environment Variables:
              WIGAI_SMOKE_TEST_MUTATIONS=true   Enable mutation tests (REQUIRED for mutations)

            Exit Codes:
              0   All checks passed
              1   One or more checks failed

            Note: Mutation tests require the WIGAI_SMOKE_TEST_MUTATIONS env var.
            CLI flags for mutations are intentionally disabled per AC4.
            """);
    }
}
