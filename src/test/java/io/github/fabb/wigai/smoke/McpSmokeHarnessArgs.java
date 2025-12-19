package io.github.fabb.wigai.smoke;

import java.util.Objects;

public record McpSmokeHarnessArgs(
        String host,
        int port,
        String endpointPath,
        boolean mutationsEnabled
) {
    public McpSmokeHarnessArgs {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(endpointPath, "endpointPath");
    }

    public String resolvedUrl() {
        String normalizedPath = endpointPath.startsWith("/") ? endpointPath : "/" + endpointPath;
        return "http://" + host + ":" + port + normalizedPath;
    }
}

