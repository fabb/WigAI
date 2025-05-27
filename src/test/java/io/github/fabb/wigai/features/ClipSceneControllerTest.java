package io.github.fabb.wigai.features;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.features.ClipSceneController.ClipLaunchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClipSceneController.
 */
class ClipSceneControllerTest {

    @Mock
    private BitwigApiFacade bitwigApiFacade;

    @Mock
    private Logger logger;

    private ClipSceneController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ClipSceneController(bitwigApiFacade, logger);
    }

    @Test
    void testLaunchClip_Success() {
        // Arrange
        String trackName = "Drums";
        int clipIndex = 0;

        when(bitwigApiFacade.findTrackByName(trackName)).thenReturn(true);
        when(bitwigApiFacade.getTrackClipCount(trackName)).thenReturn(8);
        when(bitwigApiFacade.launchClip(trackName, clipIndex)).thenReturn(true);

        // Act
        ClipLaunchResult result = controller.launchClip(trackName, clipIndex);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Clip at Drums[0] launched.", result.getMessage());
        assertNull(result.getErrorCode());

        verify(bitwigApiFacade).findTrackByName(trackName);
        verify(bitwigApiFacade).getTrackClipCount(trackName);
        verify(bitwigApiFacade).launchClip(trackName, clipIndex);
    }

    @Test
    void testLaunchClip_TrackNotFound() {
        // Arrange
        String trackName = "NonExistentTrack";
        int clipIndex = 0;

        when(bitwigApiFacade.findTrackByName(trackName)).thenReturn(false);

        // Act
        ClipLaunchResult result = controller.launchClip(trackName, clipIndex);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("TRACK_NOT_FOUND", result.getErrorCode());
        assertEquals("Track 'NonExistentTrack' not found", result.getMessage());

        verify(bitwigApiFacade).findTrackByName(trackName);
        verify(bitwigApiFacade, never()).getTrackClipCount(anyString());
        verify(bitwigApiFacade, never()).launchClip(anyString(), anyInt());
    }

    @Test
    void testLaunchClip_ClipIndexOutOfBounds() {
        // Arrange
        String trackName = "Drums";
        int clipIndex = 10;

        when(bitwigApiFacade.findTrackByName(trackName)).thenReturn(true);
        when(bitwigApiFacade.getTrackClipCount(trackName)).thenReturn(8);

        // Act
        ClipLaunchResult result = controller.launchClip(trackName, clipIndex);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("CLIP_INDEX_OUT_OF_BOUNDS", result.getErrorCode());
        assertEquals("Clip index 10 is out of bounds for track 'Drums'", result.getMessage());

        verify(bitwigApiFacade).findTrackByName(trackName);
        verify(bitwigApiFacade).getTrackClipCount(trackName);
        verify(bitwigApiFacade, never()).launchClip(anyString(), anyInt());
    }

    @Test
    void testLaunchClip_BitwigLaunchFailed() {
        // Arrange
        String trackName = "Drums";
        int clipIndex = 0;

        when(bitwigApiFacade.findTrackByName(trackName)).thenReturn(true);
        when(bitwigApiFacade.getTrackClipCount(trackName)).thenReturn(8);
        when(bitwigApiFacade.launchClip(trackName, clipIndex)).thenReturn(false);

        // Act
        ClipLaunchResult result = controller.launchClip(trackName, clipIndex);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("BITWIG_ERROR", result.getErrorCode());
        assertEquals("Failed to launch clip", result.getMessage());

        verify(bitwigApiFacade).findTrackByName(trackName);
        verify(bitwigApiFacade).getTrackClipCount(trackName);
        verify(bitwigApiFacade).launchClip(trackName, clipIndex);
    }

    @Test
    void testLaunchClip_ExceptionHandling() {
        // Arrange
        String trackName = "Drums";
        int clipIndex = 0;
        RuntimeException exception = new RuntimeException("Bitwig API error");

        when(bitwigApiFacade.findTrackByName(trackName)).thenThrow(exception);

        // Act
        ClipLaunchResult result = controller.launchClip(trackName, clipIndex);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("BITWIG_ERROR", result.getErrorCode());
        assertTrue(result.getMessage().contains("Internal error occurred while launching clip"));

        verify(bitwigApiFacade).findTrackByName(trackName);
    }

    @Test
    void testLaunchClip_EmptyTrackName() {
        // Arrange
        String trackName = "";
        int clipIndex = 0;

        when(bitwigApiFacade.findTrackByName(trackName)).thenReturn(false);

        // Act
        ClipLaunchResult result = controller.launchClip(trackName, clipIndex);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("TRACK_NOT_FOUND", result.getErrorCode());
        assertEquals("Track '' not found", result.getMessage());
    }

    @Test
    void testLaunchClip_NegativeClipIndex() {
        // This test verifies the controller can handle negative indices gracefully
        // The validation should be done at the tool level, but controller should handle it too
        String trackName = "Drums";
        int clipIndex = -1;

        when(bitwigApiFacade.findTrackByName(trackName)).thenReturn(true);
        when(bitwigApiFacade.getTrackClipCount(trackName)).thenReturn(8);

        // Act
        ClipLaunchResult result = controller.launchClip(trackName, clipIndex);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("CLIP_INDEX_OUT_OF_BOUNDS", result.getErrorCode());
        assertTrue(result.getMessage().contains("out of bounds"));
    }

    @Test
    void testLaunchClip_ValidBoundaryClipIndex() {
        // Test launching the last valid clip slot
        String trackName = "Drums";
        int clipIndex = 7; // Last slot in an 8-slot bank

        when(bitwigApiFacade.findTrackByName(trackName)).thenReturn(true);
        when(bitwigApiFacade.getTrackClipCount(trackName)).thenReturn(8);
        when(bitwigApiFacade.launchClip(trackName, clipIndex)).thenReturn(true);

        // Act
        ClipLaunchResult result = controller.launchClip(trackName, clipIndex);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Clip at Drums[7] launched.", result.getMessage());

        verify(bitwigApiFacade).launchClip(trackName, clipIndex);
    }
}
