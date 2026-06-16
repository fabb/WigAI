package io.github.fabb.wigai.features;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;

/**
 * Controller class for project-level features.
 * Bridges between MCP tools and Bitwig API operations for switching between open projects.
 */
public class ProjectController {
    private final BitwigApiFacade bitwigApiFacade;
    private final Logger logger;

    /**
     * Creates a new ProjectController instance.
     *
     * @param bitwigApiFacade The facade for Bitwig API interactions
     * @param logger          The logger for logging operations
     */
    public ProjectController(BitwigApiFacade bitwigApiFacade, Logger logger) {
        this.bitwigApiFacade = bitwigApiFacade;
        this.logger = logger;
    }

    /**
     * Switches to the next open project tab.
     *
     * @return A message indicating the operation result
     */
    public String nextProject() {
        try {
            logger.info("ProjectController: Switching to next project");
            bitwigApiFacade.nextProject();
            return "Switched to next project.";
        } catch (Exception e) {
            logger.info("ProjectController: Error switching to next project: " + e.getMessage());
            throw new BitwigApiException(ErrorCode.PROJECT_ERROR, "nextProject", "Failed to switch to next project", e);
        }
    }

    /**
     * Switches to the previous open project tab.
     *
     * @return A message indicating the operation result
     */
    public String previousProject() {
        try {
            logger.info("ProjectController: Switching to previous project");
            bitwigApiFacade.previousProject();
            return "Switched to previous project.";
        } catch (Exception e) {
            logger.info("ProjectController: Error switching to previous project: " + e.getMessage());
            throw new BitwigApiException(ErrorCode.PROJECT_ERROR, "previousProject", "Failed to switch to previous project", e);
        }
    }
}
