package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Transport;
import io.github.fabb.wigai.common.Logger;

/**
 * Facade for Bitwig API interactions.
 * This class abstracts the Bitwig API and provides simplified methods for common operations.
 */
public class BitwigApiFacade {
    private final ControllerHost host;
    private final Transport transport;
    private final Logger logger;

    /**
     * Creates a new BitwigApiFacade instance.
     *
     * @param host   The Bitwig ControllerHost
     * @param logger The logger for logging operations
     */
    public BitwigApiFacade(ControllerHost host, Logger logger) {
        this.host = host;
        this.transport = host.createTransport();
        this.logger = logger;
    }

    /**
     * Starts the transport playback.
     */
    public void startTransport() {
        logger.info("BitwigApiFacade: Starting transport playback");
        transport.play();
    }

    /**
     * Get the ControllerHost instance.
     *
     * @return The ControllerHost
     */
    public ControllerHost getHost() {
        return host;
    }
}
