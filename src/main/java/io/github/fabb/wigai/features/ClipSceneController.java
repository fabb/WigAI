package io.github.fabb.wigai.features;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;

/**
 * Controller for clip and scene launching operations in Bitwig Studio.
 * Handles the business logic for session control including clip launching,
 * track finding, and validation of clip operations.
 */
public class ClipSceneController {

    private final BitwigApiFacade bitwigApiFacade;
    private final Logger logger;

    /**
     * Constructs a ClipSceneController with required dependencies.
     *
     * @param bitwigApiFacade The facade for Bitwig API interactions
     * @param logger The logger service for operation logging
     */
    public ClipSceneController(BitwigApiFacade bitwigApiFacade, Logger logger) {
        this.bitwigApiFacade = bitwigApiFacade;
        this.logger = logger;
    }

    /**
     * Launches a clip at the specified track and clip index.
     *
     * @param trackName The name of the track containing the clip (case-sensitive)
     * @param clipIndex The zero-based index of the clip slot to launch
     * @return ClipLaunchResult indicating success/failure and any error details
     */
    public ClipLaunchResult launchClip(String trackName, int clipIndex) {
        try {
            logger.info("Attempting to launch clip - Track: '" + trackName + "', Index: " + clipIndex);

            // Find the track by name (case-sensitive)
            boolean trackFound = bitwigApiFacade.findTrackByName(trackName);
            if (!trackFound) {
                logger.warn("Track not found: '" + trackName + "'");
                return ClipLaunchResult.error("TRACK_NOT_FOUND", "Track '" + trackName + "' not found");
            }

            // Check if clip index is within bounds
            int trackClipCount = bitwigApiFacade.getTrackClipCount(trackName);
            if (clipIndex < 0 || clipIndex >= trackClipCount) {
                logger.warn("Clip index " + clipIndex + " out of bounds for track '" + trackName + "' (valid range: 0-" + (trackClipCount - 1) + ")");
                return ClipLaunchResult.error("CLIP_INDEX_OUT_OF_BOUNDS",
                    "Clip index " + clipIndex + " is out of bounds for track '" + trackName + "'");
            }

            // Launch the clip
            boolean launched = bitwigApiFacade.launchClip(trackName, clipIndex);
            if (launched) {
                logger.info("Successfully launched clip at " + trackName + "[" + clipIndex + "]");
                return ClipLaunchResult.success("Clip at " + trackName + "[" + clipIndex + "] launched.");
            } else {
                logger.error("Failed to launch clip at " + trackName + "[" + clipIndex + "]");
                return ClipLaunchResult.error("BITWIG_ERROR", "Failed to launch clip");
            }

        } catch (Exception e) {
            logger.error("Unexpected error launching clip: " + e.getMessage(), e);
            return ClipLaunchResult.error("BITWIG_ERROR", "Internal error occurred while launching clip: " + e.getMessage());
        }
    }

    /**
     * Result class for clip launch operations.
     */
    public static class ClipLaunchResult {
        private final boolean success;
        private final String errorCode;
        private final String message;

        private ClipLaunchResult(boolean success, String errorCode, String message) {
            this.success = success;
            this.errorCode = errorCode;
            this.message = message;
        }

        /**
         * Creates a successful result.
         *
         * @param message Success message
         * @return Successful ClipLaunchResult
         */
        public static ClipLaunchResult success(String message) {
            return new ClipLaunchResult(true, null, message);
        }

        /**
         * Creates an error result.
         *
         * @param errorCode Error code for the failure
         * @param message Error message
         * @return Error ClipLaunchResult
         */
        public static ClipLaunchResult error(String errorCode, String message) {
            return new ClipLaunchResult(false, errorCode, message);
        }

        /**
         * Returns whether the operation was successful.
         *
         * @return true if successful, false otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the error code if the operation failed.
         *
         * @return error code or null if successful
         */
        public String getErrorCode() {
            return errorCode;
        }

        /**
         * Returns the result message.
         *
         * @return success or error message
         */
        public String getMessage() {
            return message;
        }
    }
}
