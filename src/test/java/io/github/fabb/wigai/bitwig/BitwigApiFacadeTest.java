package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Transport;
import io.github.fabb.wigai.common.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the BitwigApiFacade class.
 */
public class BitwigApiFacadeTest {

    @Mock
    private ControllerHost mockHost;

    @Mock
    private Transport mockTransport;

    @Mock
    private Logger mockLogger;

    private BitwigApiFacade bitwigApiFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockHost.createTransport()).thenReturn(mockTransport);
        bitwigApiFacade = new BitwigApiFacade(mockHost, mockLogger);
    }

    @Test
    void testStartTransport() {
        // Call the method
        bitwigApiFacade.startTransport();

        // Verify the transport's play method was called
        verify(mockTransport).play();

        // Verify logging
        verify(mockLogger).info("BitwigApiFacade: Starting transport playback");
    }
}
