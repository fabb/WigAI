package io.github.fabb.wigai.features;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;

/**
 * Controller class for transport control features.
 * Bridges between MCP tools and Bitwig API operations for transport control.
 */
public class TransportController {
    private final BitwigApiFacade bitwigApiFacade;
    private final Logger logger;

    /**
     * Creates a new TransportController instance.
     *
     * @param bitwigApiFacade The facade for Bitwig API interactions
     * @param logger          The logger for logging operations
     */
    public TransportController(BitwigApiFacade bitwigApiFacade, Logger logger) {
        this.bitwigApiFacade = bitwigApiFacade;
        this.logger = logger;
    }

    /**
     * Starts the transport playback.
     *
     * @return A message indicating the operation result
     */
    public String startTransport() {
        try {
            logger.info("TransportController: Starting transport playback");
            bitwigApiFacade.startTransport();
            return "Transport playback started.";
        } catch (Exception e) {
            logger.info("TransportController: Error starting transport playback: " + e.getMessage());
            throw new RuntimeException("Failed to start transport playback", e);
        }
    }

    /**
     * Stops the transport playback.
     *
     * @return A message indicating the operation result
     */
    public String stopTransport() {
        try {
            logger.info("TransportController: Stopping transport playback");
            bitwigApiFacade.stopTransport();
            return "Transport playback stopped.";
        } catch (Exception e) {
            logger.info("TransportController: Error stopping transport playback: " + e.getMessage());
            throw new RuntimeException("Failed to stop transport playback", e);
        }
    }
}
