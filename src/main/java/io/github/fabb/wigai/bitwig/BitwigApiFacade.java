package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.common.error.WigAIErrorHandler;
import io.github.fabb.wigai.common.validation.ParameterValidator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade for Bitwig API interactions.
 * This class abstracts the Bitwig API and provides simplified methods for common operations.
 */
public class BitwigApiFacade {
    private final ControllerHost host;
    private final Transport transport;
    private final Application application;
    private final Logger logger;
    private final CursorDevice cursorDevice;
    private final RemoteControlsPage deviceParameterBank;
    private final TrackBank trackBank;
    private final SceneBankFacade sceneBankFacade;
    private final CursorTrack cursorTrack;
    private final RemoteControlsPage projectParameterBank;

    /**
     * Creates a new BitwigApiFacade instance.
     *
     * @param host   The Bitwig ControllerHost
     * @param logger The logger for logging operations
     */
    public BitwigApiFacade(ControllerHost host, Logger logger) {
        this.host = host;
        this.transport = host.createTransport();
        this.application = host.createApplication();
        this.logger = logger;

        // Mark transport properties as interested for status queries
        transport.isPlaying().markInterested();
        transport.isArrangerRecordEnabled().markInterested();
        transport.isArrangerLoopEnabled().markInterested();
        transport.isMetronomeEnabled().markInterested();
        transport.tempo().value().markInterested();
        transport.timeSignature().markInterested();
        transport.getPosition().markInterested();
        transport.playPositionInSeconds().markInterested();

        // Mark application properties as interested for status queries
        application.projectName().markInterested();
        application.hasActiveEngine().markInterested();

        // Initialize device control - use CursorTrack.createCursorDevice() instead of deprecated host.createCursorDevice()
        this.cursorTrack = host.createCursorTrack(0, 0);
        this.cursorDevice = cursorTrack.createCursorDevice();
        this.deviceParameterBank = cursorDevice.createCursorRemoteControlsPage(8);

        // Initialize project parameter access via MasterTrack (project parameters)
        MasterTrack masterTrack = host.createMasterTrack(0);
        this.projectParameterBank = masterTrack.createCursorRemoteControlsPage(8);

        // Initialize track bank for clip launching (8 tracks, 8 scenes for now, but needs to be increased later for full functionality)
        this.trackBank = host.createTrackBank(8, 0, 8);
        this.sceneBankFacade = new SceneBankFacade(host, logger, 8); // 8 scenes for MVP, but needs to be increased later for full functionality

        // Mark interest in device properties to enable value access
        cursorDevice.exists().markInterested();
        cursorDevice.name().markInterested();
        cursorDevice.isEnabled().markInterested();

        // Mark interest in all device parameter properties to enable value access
        for (int i = 0; i < 8; i++) {
            RemoteControl parameter = deviceParameterBank.getParameter(i);
            parameter.name().markInterested();
            parameter.value().markInterested();
            parameter.displayedValue().markInterested();
        }

        // Mark interest in project parameters to enable value access
        for (int i = 0; i < 8; i++) {
            RemoteControl parameter = projectParameterBank.getParameter(i);
            parameter.exists().markInterested();
            parameter.name().markInterested();
            parameter.value().markInterested();
            parameter.displayedValue().markInterested();
        }

        // Mark interest in cursor track properties for selected track details
        cursorTrack.exists().markInterested();
        cursorTrack.name().markInterested();
        cursorTrack.trackType().markInterested();
        cursorTrack.isGroup().markInterested();
        cursorTrack.mute().markInterested();
        cursorTrack.solo().markInterested();
        cursorTrack.arm().markInterested();

        // Mark interest in track properties for clip launching
        for (int trackIndex = 0; trackIndex < 8; trackIndex++) {
            Track track = trackBank.getItemAt(trackIndex);
            track.name().markInterested();
            track.exists().markInterested();

            ClipLauncherSlotBank trackSlots = track.clipLauncherSlotBank();
            for (int slotIndex = 0; slotIndex < 8; slotIndex++) {
                ClipLauncherSlot slot = trackSlots.getItemAt(slotIndex);
                slot.hasContent().markInterested();
                slot.isPlaying().markInterested();
            }
        }
    }

    /**
     * Returns the number of tracks in the track bank.
     *
     * @return the size of the track bank
     */
    public int getTrackBankSize() {
        return trackBank.getSizeOfBank();
    }

    /**
     * Returns the name of the track at the given index.
     *
     * @param index the track index
     * @return the track name
     * @throws BitwigApiException if the index is invalid or track doesn't exist
     */
    public String getTrackNameByIndex(int index) throws BitwigApiException {
        final String operation = "getTrackNameByIndex";

        return WigAIErrorHandler.executeWithErrorHandling(operation, () -> {
            // Validate track index
            if (index < 0 || index >= trackBank.getSizeOfBank()) {
                throw new BitwigApiException(
                    ErrorCode.INVALID_RANGE,
                    operation,
                    "Track index must be between 0 and " + (trackBank.getSizeOfBank() - 1) + ", got: " + index,
                    Map.of("index", index, "max_index", trackBank.getSizeOfBank() - 1)
                );
            }

            Track track = trackBank.getItemAt(index);
            if (!track.exists().get()) {
                throw new BitwigApiException(
                    ErrorCode.TRACK_NOT_FOUND,
                    operation,
                    "Track at index " + index + " does not exist",
                    Map.of("index", index)
                );
            }

            return track.name().get();
        });
    }

    /**
     * Starts the transport playback.
     */
    public void startTransport() {
        logger.info("BitwigApiFacade: Starting transport playback");
        transport.play();
    }

    /**
     * Stops the transport playback.
     */
    public void stopTransport() {
        logger.info("BitwigApiFacade: Stopping transport playback");
        transport.stop();
    }

    /**
     * Get the ControllerHost instance.
     *
     * @return The ControllerHost
     */
    public ControllerHost getHost() {
        return host;
    }

    /**
     * Checks if a device is currently selected.
     *
     * @return true if a device is selected, false otherwise
     */
    public boolean isDeviceSelected() {
        logger.info("BitwigApiFacade: Checking if device is selected");
        return cursorDevice.exists().get();
    }

    /**
     * Gets the name of the currently selected device.
     *
     * @return The device name
     * @throws BitwigApiException if no device is selected
     */
    public String getSelectedDeviceName() throws BitwigApiException {
        final String operation = "getSelectedDeviceName";
        logger.info("BitwigApiFacade: Getting selected device name");

        return WigAIErrorHandler.executeWithErrorHandling(operation, () -> {
            if (!isDeviceSelected()) {
                throw new BitwigApiException(
                    ErrorCode.DEVICE_NOT_SELECTED,
                    operation,
                    "No device is currently selected"
                );
            }
            return cursorDevice.name().get();
        });
    }

    /**
     * Gets the parameters of the currently selected device.
     *
     * @return A list of ParameterInfo objects representing the 8 addressable parameters
     */
    public List<ParameterInfo> getSelectedDeviceParameters() {
        logger.info("BitwigApiFacade: Getting selected device parameters");
        List<ParameterInfo> parameters = new ArrayList<>();

        if (!isDeviceSelected()) {
            logger.info("BitwigApiFacade: No device selected, returning empty parameters list");
            return parameters;
        }

        for (int i = 0; i < 8; i++) {
            RemoteControl parameter = deviceParameterBank.getParameter(i);
            String name = parameter.name().get();
            double value = parameter.value().get();
            String displayValue = parameter.displayedValue().get();

            // Handle null or empty names
            if (name != null && name.trim().isEmpty()) {
                name = null;
            }

            parameters.add(new ParameterInfo(i, name, value, displayValue));
        }

        logger.info("BitwigApiFacade: Retrieved " + parameters.size() + " parameters");
        return parameters;
    }

    /**
     * Sets the value of a specific parameter for the currently selected device.
     *
     * @param parameterIndex The index of the parameter to set (0-7)
     * @param value          The value to set (0.0-1.0)
     * @throws BitwigApiException if parameterIndex is out of range, value is out of range, no device is selected, or Bitwig API error occurs
     */
    public void setSelectedDeviceParameter(int parameterIndex, double value) throws BitwigApiException {
        final String operation = "setSelectedDeviceParameter";
        logger.info("BitwigApiFacade: Setting parameter " + parameterIndex + " to " + value);

        WigAIErrorHandler.executeWithErrorHandling(operation, () -> {
            // Validate parameter index
            ParameterValidator.validateParameterIndex(parameterIndex, operation);

            // Validate value range
            ParameterValidator.validateParameterValue(value, operation);

            // Check if device is selected
            if (!isDeviceSelected()) {
                throw new BitwigApiException(
                    ErrorCode.DEVICE_NOT_SELECTED,
                    operation,
                    "No device is currently selected"
                );
            }

            // Set the parameter value
            RemoteControl parameter = deviceParameterBank.getParameter(parameterIndex);
            parameter.value().set(value);

            logger.info("BitwigApiFacade: Successfully set parameter " + parameterIndex + " to " + value);
        });
    }

    /**
     * Finds a track by name using case-sensitive matching.
     *
     * @param trackName The name of the track to find
     * @return The track index if found
     * @throws BitwigApiException if the track is not found
     */
    public int findTrackIndexByName(String trackName) throws BitwigApiException {
        final String operation = "findTrackIndexByName";
        logger.info("BitwigApiFacade: Searching for track '" + trackName + "'");

        return WigAIErrorHandler.executeWithErrorHandling(operation, () -> {
            ParameterValidator.validateNotEmpty(trackName, "trackName", operation);

            for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
                Track track = trackBank.getItemAt(i);
                if (track.exists().get()) {
                    String currentTrackName = track.name().get();
                    if (trackName.equals(currentTrackName)) {
                        logger.info("BitwigApiFacade: Found track '" + trackName + "' at index " + i);
                        return i;
                    }
                }
            }

            throw new BitwigApiException(
                ErrorCode.TRACK_NOT_FOUND,
                operation,
                "Track '" + trackName + "' not found",
                Map.of("trackName", trackName)
            );
        });
    }

    /**
     * Checks if a track exists by name using case-sensitive matching.
     *
     * @param trackName The name of the track to check
     * @return true if the track exists, false otherwise
     */
    public boolean trackExists(String trackName) {
        try {
            findTrackIndexByName(trackName);
            return true;
        } catch (BitwigApiException e) {
            return false;
        }
    }

    /**
     * Gets the number of clip slots available for a track.
     *
     * @param trackName The name of the track
     * @return The number of clip slots, or 0 if track not found
     */
    public int getTrackClipCount(String trackName) {
        logger.info("BitwigApiFacade: Getting clip count for track '" + trackName + "'");

        for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
            Track track = trackBank.getItemAt(i);
            if (track.exists().get() && trackName.equals(track.name().get())) {
                // Return the number of available clip launcher slots (8 for MVP)
                return track.clipLauncherSlotBank().getSizeOfBank();
            }
        }

        logger.warn("BitwigApiFacade: Track '" + trackName + "' not found for clip count check");
        return 0;
    }

    /**
     * Launches a clip at the specified track and clip index.
     *
     * @param trackName The name of the track containing the clip
     * @param clipIndex The zero-based index of the clip slot to launch
     * @throws BitwigApiException if track is not found, clip index is invalid, or launch fails
     */
    public void launchClip(String trackName, int clipIndex) throws BitwigApiException {
        final String operation = "launchClip";
        logger.info("BitwigApiFacade: Launching clip at " + trackName + "[" + clipIndex + "]");

        WigAIErrorHandler.executeWithErrorHandling(operation, () -> {
            // Validate parameters
            ParameterValidator.validateNotEmpty(trackName, "trackName", operation);
            ParameterValidator.validateClipIndex(clipIndex, operation);

            // Find the track
            Track targetTrack = null;
            for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
                Track track = trackBank.getItemAt(i);
                if (track.exists().get() && trackName.equals(track.name().get())) {
                    targetTrack = track;
                    break;
                }
            }

            if (targetTrack == null) {
                throw new BitwigApiException(
                    ErrorCode.TRACK_NOT_FOUND,
                    operation,
                    "Track '" + trackName + "' not found",
                    Map.of("trackName", trackName)
                );
            }

            // Validate clip index within track bounds
            ClipLauncherSlotBank slotBank = targetTrack.clipLauncherSlotBank();
            if (clipIndex >= slotBank.getSizeOfBank()) {
                throw new BitwigApiException(
                    ErrorCode.INVALID_RANGE,
                    operation,
                    "Clip index " + clipIndex + " out of bounds for track '" + trackName + "' (max: " + (slotBank.getSizeOfBank() - 1) + ")",
                    Map.of("trackName", trackName, "clipIndex", clipIndex, "maxIndex", slotBank.getSizeOfBank() - 1)
                );
            }

            // Launch the clip
            ClipLauncherSlot slot = slotBank.getItemAt(clipIndex);
            slot.launch();

            logger.info("BitwigApiFacade: Successfully launched clip at " + trackName + "[" + clipIndex + "]");
        });
    }

    /**
     * Finds the first scene index with the given name (case-sensitive).
     * Returns -1 if not found.
     */
    public int findSceneByName(String sceneName) {
        return sceneBankFacade.findSceneByName(sceneName);
    }

    /**
     * Gets the name of the scene at the given index, or null if not present.
     */
    public String getSceneName(int index) {
        return sceneBankFacade.getSceneName(index);
    }

    /**
     * Gets the number of scenes in the scene bank.
     */
    public int getSceneCount() {
        return sceneBankFacade.getSceneCount();
    }

    /**
     * Gets the current project name.
     *
     * @return The project name or "Unknown Project" if not available
     */
    public String getProjectName() {
        logger.info("BitwigApiFacade: Getting project name");
        try {
            String projectName = application.projectName().get();
            return projectName != null && !projectName.trim().isEmpty() ? projectName : "Unknown Project";
        } catch (Exception e) {
            logger.warn("BitwigApiFacade: Unable to get project name: " + e.getMessage());
            return "Unknown Project";
        }
    }

    /**
     * Checks if the audio engine is currently active.
     *
     * @return true if the audio engine is active, false otherwise
     */
    public boolean isAudioEngineActive() {
        logger.info("BitwigApiFacade: Checking audio engine status");
        try {
            return application.hasActiveEngine().get();
        } catch (Exception e) {
            logger.warn("BitwigApiFacade: Unable to get audio engine status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Formats seconds into a time string in the format MM:SS.mmm or HH:MM:SS.mmm
     * @param seconds The time in seconds
     * @return Formatted time string with milliseconds
     */
    private String formatTimeString(double seconds) {
        try {
            int totalSeconds = (int) Math.floor(seconds);
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int secs = totalSeconds % 60;

            // Calculate milliseconds from the fractional part
            int milliseconds = (int) Math.round((seconds - Math.floor(seconds)) * 1000);

            // Handle edge case where rounding gives us 1000ms
            if (milliseconds >= 1000) {
                milliseconds = 0;
                secs += 1;
                if (secs >= 60) {
                    secs = 0;
                    minutes += 1;
                    if (minutes >= 60) {
                        minutes = 0;
                        hours += 1;
                    }
                }
            }

            if (hours > 0) {
                return String.format("%d:%02d:%02d.%03d", hours, minutes, secs, milliseconds);
            } else {
                return String.format("%d:%02d.%03d", minutes, secs, milliseconds);
            }
        } catch (Exception e) {
            return "0:00.000";
        }
    }

    /**
     * Gets the current transport status information.
     *
     * @return A map containing transport status data
     */
    public java.util.Map<String, Object> getTransportStatus() {
        logger.info("BitwigApiFacade: Getting transport status");
        java.util.Map<String, Object> transportMap = new java.util.LinkedHashMap<>();

        try {
            transportMap.put("playing", transport.isPlaying().get());
            transportMap.put("recording", transport.isArrangerRecordEnabled().get());
            transportMap.put("loop_active", transport.isArrangerLoopEnabled().get());
            transportMap.put("metronome_active", transport.isMetronomeEnabled().get());
            transportMap.put("current_tempo", transport.tempo().getRaw());
            transportMap.put("time_signature", transport.timeSignature().get());

            // Format position as Bitwig-style beat string
            double positionInBeats = transport.getPosition().get();
            String beatStr = formatBitwigBeatPosition(positionInBeats);
            transportMap.put("current_beat_str", beatStr);

            // Get time string using playPositionInSeconds
            double positionInSeconds = transport.playPositionInSeconds().get();
            String timeStr = formatTimeString(positionInSeconds);
            transportMap.put("current_time_str", timeStr);
        } catch (Exception e) {
            logger.warn("BitwigApiFacade: Unable to get complete transport status: " + e.getMessage());
            // Provide default values if API calls fail
            transportMap.put("playing", false);
            transportMap.put("recording", false);
            transportMap.put("loop_active", false);
            transportMap.put("metronome_active", false);
            transportMap.put("current_tempo", 120.0);
            transportMap.put("time_signature", "4/4");
            transportMap.put("current_beat_str", "1.1.1:0");
            transportMap.put("current_time_str", "0:00.000");
        }

        return transportMap;
    }

    /**
     * Formats a position in beats to Bitwig-style format: measures.beats.sixteenths:ticks
     * Example: 1.1.1:0 = measure 1, beat 1, sixteenth 1, tick 0
     */
    private String formatBitwigBeatPosition(double positionInBeats) {
        try {
            // Assume 4/4 time signature for calculation
            int beatsPerMeasure = 4;
            int sixteenthsPerBeat = 4;
            int ticksPerSixteenth = 240; // Common MIDI resolution

            // Convert beats to total ticks
            int totalTicks = (int) Math.round(positionInBeats * sixteenthsPerBeat * ticksPerSixteenth);

            // Calculate measures (1-based)
            int measures = (totalTicks / (beatsPerMeasure * sixteenthsPerBeat * ticksPerSixteenth)) + 1;
            int remainingTicks = totalTicks % (beatsPerMeasure * sixteenthsPerBeat * ticksPerSixteenth);

            // Calculate beats within measure (1-based)
            int beats = (remainingTicks / (sixteenthsPerBeat * ticksPerSixteenth)) + 1;
            remainingTicks = remainingTicks % (sixteenthsPerBeat * ticksPerSixteenth);

            // Calculate sixteenths within beat (1-based)
            int sixteenths = (remainingTicks / ticksPerSixteenth) + 1;
            int ticks = remainingTicks % ticksPerSixteenth;

            return String.format("%d.%d.%d:%d", measures, beats, sixteenths, ticks);
        } catch (Exception e) {
            logger.warn("BitwigApiFacade: Error formatting beat position: " + e.getMessage());
            return "1.1.1:0";
        }
    }

    /**
     * Gets the project parameters (0-7) from the project's remote controls page.
     * Only returns parameters where exists() is true.
     *
     * @return A list of ParameterInfo objects representing the existing project parameters
     */
    public List<ParameterInfo> getProjectParameters() {
        logger.info("BitwigApiFacade: Getting project parameters");
        List<ParameterInfo> parameters = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            RemoteControl parameter = projectParameterBank.getParameter(i);
            boolean exists = parameter.exists().get();

            if (exists) {
                String name = parameter.name().get();
                double value = parameter.value().get();
                String displayValue = parameter.displayedValue().get();

                // Handle null or empty names
                if (name != null && name.trim().isEmpty()) {
                    name = null;
                }

                parameters.add(new ParameterInfo(i, name, value, displayValue));
            }
        }

        logger.info("BitwigApiFacade: Retrieved " + parameters.size() + " existing project parameters");
        return parameters;
    }

    /**
     * Gets information about the currently selected track.
     *
     * @return A map containing selected track information, or null if no track is selected
     */
    public Map<String, Object> getSelectedTrackInfo() {
        logger.info("BitwigApiFacade: Getting selected track information");

        if (!cursorTrack.exists().get()) {
            logger.info("BitwigApiFacade: No track selected");
            return null;
        }

        Map<String, Object> trackInfo = new LinkedHashMap<>();

        try {
            // Get track index by finding it in the track bank
            int trackIndex = -1;
            String trackName = cursorTrack.name().get();
            for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
                Track track = trackBank.getItemAt(i);
                if (track.exists().get() && trackName.equals(track.name().get())) {
                    trackIndex = i;
                    break;
                }
            }

            trackInfo.put("index", trackIndex);
            trackInfo.put("name", trackName);
            trackInfo.put("type", cursorTrack.trackType().get().toLowerCase());
            trackInfo.put("is_group", cursorTrack.isGroup().get());
            trackInfo.put("muted", cursorTrack.mute().get());
            trackInfo.put("soloed", cursorTrack.solo().get());
            trackInfo.put("armed", cursorTrack.arm().get());

            logger.info("BitwigApiFacade: Retrieved selected track info: " + trackName);
        } catch (Exception e) {
            logger.warn("BitwigApiFacade: Error getting selected track info: " + e.getMessage());
            return null;
        }

        return trackInfo;
    }

    /**
     * Gets information about the currently selected device including track context, device info, and parameters.
     *
     * @return A map containing selected device information, or null if no device is selected
     */
    public Map<String, Object> getSelectedDeviceInfo() {
        logger.info("BitwigApiFacade: Getting selected device information");

        if (!cursorDevice.exists().get()) {
            logger.info("BitwigApiFacade: No device selected");
            return null;
        }

        Map<String, Object> deviceInfo = new LinkedHashMap<>();

        try {
            // Get track information where the device is located
            String trackName = cursorTrack.name().get();
            int trackIndex = -1;

            // Find track index in the track bank
            for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
                Track track = trackBank.getItemAt(i);
                if (track.exists().get() && trackName.equals(track.name().get())) {
                    trackIndex = i;
                    break;
                }
            }

            deviceInfo.put("track_name", trackName);
            deviceInfo.put("track_index", trackIndex);

            // Get device position/index in the device chain
            // Note: Bitwig API doesn't directly expose device index in chain, so we use 0 as default
            // This could be enhanced in the future with more complex logic to determine actual position
            deviceInfo.put("index", 0);

            // Get device name and bypass status
            deviceInfo.put("name", cursorDevice.name().get());
            deviceInfo.put("bypassed", !cursorDevice.isEnabled().get());

            // Get device parameters
            List<Map<String, Object>> parametersArray = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                RemoteControl parameter = deviceParameterBank.getParameter(i);
                String name = parameter.name().get();

                // Only include parameters that exist and have names
                if (name != null && !name.trim().isEmpty()) {
                    Map<String, Object> paramMap = new LinkedHashMap<>();
                    paramMap.put("index", i);
                    paramMap.put("name", name);
                    paramMap.put("value", parameter.value().get());
                    paramMap.put("display_value", parameter.displayedValue().get());
                    parametersArray.add(paramMap);
                }
            }
            deviceInfo.put("parameters", parametersArray);

            logger.info("BitwigApiFacade: Retrieved selected device info: " + cursorDevice.name().get());
        } catch (Exception e) {
            logger.warn("BitwigApiFacade: Error getting selected device info: " + e.getMessage());
            return null;
        }

        return deviceInfo;
    }
}
