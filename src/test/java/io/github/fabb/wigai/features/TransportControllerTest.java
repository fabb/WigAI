package io.github.fabb.wigai.features;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TransportController class.
 */
public class TransportControllerTest {

    @Mock
    private BitwigApiFacade mockBitwigApiFacade;

    @Mock
    private Logger mockLogger;

    private TransportController transportController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transportController = new TransportController(mockBitwigApiFacade, mockLogger);
    }

    @Test
    void testStartTransport() {
        // Execute the controller method
        String result = transportController.startTransport();

        // Verify the result
        assertEquals("Transport playback started.", result);

        // Verify the bitwigApiFacade was called
        verify(mockBitwigApiFacade).startTransport();

        // Verify logging
        verify(mockLogger).info("TransportController: Starting transport playback");
    }

    @Test
    void testStartTransportWithException() {
        // Setup the mock to throw an exception
        doThrow(new RuntimeException("Test exception")).when(mockBitwigApiFacade).startTransport();

        // Execute and verify exception is thrown
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transportController.startTransport();
        });

        // Verify exception message
        assertTrue(exception.getMessage().contains("Failed to start transport playback"));

        // Verify logging
        verify(mockLogger).info("TransportController: Starting transport playback");
        verify(mockLogger).info(contains("TransportController: Error starting transport playback"));
    }
}
