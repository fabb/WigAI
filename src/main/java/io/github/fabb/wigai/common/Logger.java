package io.github.fabb.wigai.common;

import com.bitwig.extension.controller.api.ControllerHost;

/**
 * Simple logger implementation for the WigAI extension.
 * Uses Bitwig's ControllerHost.println for logging.
 */
public class Logger {
    private final ControllerHost host;

    /**
     * Creates a new Logger instance.
     *
     * @param host The Bitwig ControllerHost to use for logging
     */
    public Logger(ControllerHost host) {
        if (host == null) {
            throw new IllegalArgumentException("ControllerHost cannot be null");
        }
        this.host = host;
    }

    /**
     * Log an informational message.
     *
     * @param message The message to log
     */
    public void info(String message) {
        host.println("[INFO] " + message);
    }

    /**
     * Log a warning message.
     *
     * @param message The message to log
     */
    public void warn(String message) {
        host.println("[WARN] " + message);
    }

    /**
     * Log an error message.
     *
     * @param message The message to log
     */
    public void error(String message) {
        host.println("[ERROR] " + message);
    }

    /**
     * Log a debug message.
     *
     * @param message The message to log
     */
    public void debug(String message) {
        host.println("[DEBUG] " + message);
    }

    /**
     * Log an exception with an error message.
     *
     * @param message The error message
     * @param e       The exception to log
     */
    public void error(String message, Throwable e) {
        host.println("[ERROR] " + message + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
        // Print stack trace in a Bitwig-console friendly format
        for (StackTraceElement element : e.getStackTrace()) {
            host.println("    at " + element.toString());
        }
    }
}
