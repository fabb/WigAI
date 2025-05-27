package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.features.ClipSceneController;
import io.github.fabb.wigai.features.ClipSceneController.ClipLaunchResult;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClipTool.
 */
class ClipToolTest {

    @Mock
    private ClipSceneController clipSceneController;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLaunchClipSpecification() {
        // Act
        var specification = ClipTool.launchClipSpecification(clipSceneController, logger);

        // Assert
        assertNotNull(specification);
        assertNotNull(specification.tool());
        assertEquals("launch_clip", specification.tool().name());
        assertEquals("Launch a specific clip in Bitwig by providing track name and clip slot index",
                     specification.tool().description());
        assertNotNull(specification.tool().inputSchema());
    }

    @Test
    void testHandleLaunchClip_Success() {
        // Arrange
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("track_name", "Drums");
        arguments.put("clip_index", 0);

        ClipLaunchResult successResult = ClipLaunchResult.success("Clip at Drums[0] launched.");
        when(clipSceneController.launchClip("Drums", 0)).thenReturn(successResult);

        // Initialize the handler by calling specification
        ClipTool.launchClipSpecification(clipSceneController, logger);

        // Act
        McpSchema.CallToolResult result = ClipTool.getHandler().apply(null, arguments);

        // Assert
        assertNotNull(result);
        assertFalse(result.isError());

        // Check content structure (following StatusTool pattern)
        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertEquals("Clip at Drums[0] launched.", textContent.text());

        verify(clipSceneController).launchClip("Drums", 0);
    }

    @Test
    void testHandleLaunchClip_TrackNotFound() {
        // Arrange
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("track_name", "NonExistentTrack");
        arguments.put("clip_index", 0);

        ClipLaunchResult errorResult = ClipLaunchResult.error("TRACK_NOT_FOUND", "Track 'NonExistentTrack' not found");
        when(clipSceneController.launchClip("NonExistentTrack", 0)).thenReturn(errorResult);

        // Initialize the handler by calling specification
        ClipTool.launchClipSpecification(clipSceneController, logger);

        // Act
        McpSchema.CallToolResult result = ClipTool.getHandler().apply(null, arguments);

        // Assert
        assertNotNull(result);
        assertTrue(result.isError());

        // Check content structure for error
        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertTrue(textContent.text().contains("Track 'NonExistentTrack' not found"));

        verify(clipSceneController).launchClip("NonExistentTrack", 0);
    }

    @Test
    void testHandleLaunchClip_ClipIndexOutOfBounds() {
        // Arrange
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("track_name", "Drums");
        arguments.put("clip_index", 10);

        ClipLaunchResult errorResult = ClipLaunchResult.error("CLIP_INDEX_OUT_OF_BOUNDS",
                                                            "Clip index 10 is out of bounds for track 'Drums'");
        when(clipSceneController.launchClip("Drums", 10)).thenReturn(errorResult);

        // Initialize the handler by calling specification
        ClipTool.launchClipSpecification(clipSceneController, logger);

        // Act
        McpSchema.CallToolResult result = ClipTool.getHandler().apply(null, arguments);

        // Assert
        assertNotNull(result);
        assertTrue(result.isError());

        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertTrue(textContent.text().contains("out of bounds"));

        verify(clipSceneController).launchClip("Drums", 10);
    }

    @Test
    void testHandleLaunchClip_MissingTrackName() {
        // Arrange
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("clip_index", 0);
        // Missing track_name

        // Initialize the handler by calling specification
        ClipTool.launchClipSpecification(clipSceneController, logger);

        // Act
        McpSchema.CallToolResult result = ClipTool.getHandler().apply(null, arguments);

        // Assert
        assertNotNull(result);
        assertTrue(result.isError());

        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertTrue(textContent.text().contains("Missing required parameter: track_name"));

        verify(clipSceneController, never()).launchClip(anyString(), anyInt());
    }

    @Test
    void testHandleLaunchClip_MissingClipIndex() {
        // Arrange
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("track_name", "Drums");
        // Missing clip_index

        // Initialize the handler by calling specification
        ClipTool.launchClipSpecification(clipSceneController, logger);

        // Act
        McpSchema.CallToolResult result = ClipTool.getHandler().apply(null, arguments);

        // Assert
        assertNotNull(result);
        assertTrue(result.isError());

        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertTrue(textContent.text().contains("Missing required parameter: clip_index"));

        verify(clipSceneController, never()).launchClip(anyString(), anyInt());
    }

    @Test
    void testHandleLaunchClip_InvalidTrackNameType() {
        // Arrange
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("track_name", 123); // Invalid type - should be string
        arguments.put("clip_index", 0);

        // Initialize the handler by calling specification
        ClipTool.launchClipSpecification(clipSceneController, logger);

        // Act
        McpSchema.CallToolResult result = ClipTool.getHandler().apply(null, arguments);

        // Assert
        assertNotNull(result);
        assertTrue(result.isError());

        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertTrue(textContent.text().contains("track_name must be a string"));

        verify(clipSceneController, never()).launchClip(anyString(), anyInt());
    }

    @Test
    void testHandleLaunchClip_InvalidClipIndexType() {
        // Arrange
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("track_name", "Drums");
        arguments.put("clip_index", "invalid"); // Invalid type - should be integer

        // Initialize the handler by calling specification
        ClipTool.launchClipSpecification(clipSceneController, logger);

        // Act
        McpSchema.CallToolResult result = ClipTool.getHandler().apply(null, arguments);

        // Assert
        assertNotNull(result);
        assertTrue(result.isError());

        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertTrue(textContent.text().contains("clip_index must be an integer"));

        verify(clipSceneController, never()).launchClip(anyString(), anyInt());
    }

    @Test
    void testHandleLaunchClip_EmptyTrackName() {
        // Arrange
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("track_name", "");
        arguments.put("clip_index", 0);

        // Initialize the handler by calling specification
        ClipTool.launchClipSpecification(clipSceneController, logger);

        // Act
        McpSchema.CallToolResult result = ClipTool.getHandler().apply(null, arguments);

        // Assert
        assertNotNull(result);
        assertTrue(result.isError());

        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertTrue(textContent.text().contains("track_name cannot be empty"));

        verify(clipSceneController, never()).launchClip(anyString(), anyInt());
    }

    @Test
    void testHandleLaunchClip_NegativeClipIndex() {
        // Arrange
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("track_name", "Drums");
        arguments.put("clip_index", -1);

        // Initialize the handler by calling specification
        ClipTool.launchClipSpecification(clipSceneController, logger);

        // Act
        McpSchema.CallToolResult result = ClipTool.getHandler().apply(null, arguments);

        // Assert
        assertNotNull(result);
        assertTrue(result.isError());

        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertTrue(textContent.text().contains("clip_index must be non-negative"));

        verify(clipSceneController, never()).launchClip(anyString(), anyInt());
    }

    @Test
    void testHandleLaunchClip_DoubleClipIndex() {
        // Test that double values are properly converted to integers
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("track_name", "Drums");
        arguments.put("clip_index", 2.0); // Double value that should be converted to int

        ClipLaunchResult successResult = ClipLaunchResult.success("Clip at Drums[2] launched.");
        when(clipSceneController.launchClip("Drums", 2)).thenReturn(successResult);

        // Initialize the handler by calling specification
        ClipTool.launchClipSpecification(clipSceneController, logger);

        // Act
        McpSchema.CallToolResult result = ClipTool.getHandler().apply(null, arguments);

        // Assert
        assertNotNull(result);
        assertFalse(result.isError());

        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        // Just verify it's not an error response for this test

        verify(clipSceneController).launchClip("Drums", 2);
    }
}
