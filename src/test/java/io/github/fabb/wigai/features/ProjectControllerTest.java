package io.github.fabb.wigai.features;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ProjectController class.
 */
public class ProjectControllerTest {

    @Mock
    private BitwigApiFacade mockBitwigApiFacade;

    @Mock
    private Logger mockLogger;

    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        projectController = new ProjectController(mockBitwigApiFacade, mockLogger);
    }

    @Test
    void testNextProject() {
        String result = projectController.nextProject();

        assertEquals("Switched to next project.", result);
        verify(mockBitwigApiFacade).nextProject();
        verify(mockLogger).info("ProjectController: Switching to next project");
    }

    @Test
    void testNextProjectWithException() {
        doThrow(new RuntimeException("Test exception")).when(mockBitwigApiFacade).nextProject();

        BitwigApiException exception = assertThrows(BitwigApiException.class, () -> {
            projectController.nextProject();
        });

        assertEquals(ErrorCode.PROJECT_ERROR, exception.getErrorCode());
        assertEquals("nextProject", exception.getOperation());
        assertTrue(exception.getMessage().contains("Failed to switch to next project"));

        verify(mockLogger).info("ProjectController: Switching to next project");
        verify(mockLogger).info(contains("ProjectController: Error switching to next project"));
    }

    @Test
    void testPreviousProject() {
        String result = projectController.previousProject();

        assertEquals("Switched to previous project.", result);
        verify(mockBitwigApiFacade).previousProject();
        verify(mockLogger).info("ProjectController: Switching to previous project");
    }

    @Test
    void testPreviousProjectWithException() {
        doThrow(new RuntimeException("Test exception")).when(mockBitwigApiFacade).previousProject();

        BitwigApiException exception = assertThrows(BitwigApiException.class, () -> {
            projectController.previousProject();
        });

        assertEquals(ErrorCode.PROJECT_ERROR, exception.getErrorCode());
        assertEquals("previousProject", exception.getOperation());
        assertTrue(exception.getMessage().contains("Failed to switch to previous project"));

        verify(mockLogger).info("ProjectController: Switching to previous project");
        verify(mockLogger).info(contains("ProjectController: Error switching to previous project"));
    }
}
