package io.github.fabb.wigai.smoke;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Story 1.1 host-required smoke harness runner (client-side).
 *
 * ATDD note: This class intentionally starts as NOT IMPLEMENTED so ATDD tests are RED.
 */
public final class McpSmokeHarness {

    public int run(McpSmokeHarnessArgs args, McpClient client, PrintStream out, PrintStream err) {
        Objects.requireNonNull(args, "args");
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(err, "err");

        throw new UnsupportedOperationException("ATDD RED: McpSmokeHarness not implemented yet (Story 1.1)");
    }
}

