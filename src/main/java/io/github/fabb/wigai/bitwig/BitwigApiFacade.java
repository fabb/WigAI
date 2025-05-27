package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade for Bitwig API interactions.
 * This class abstracts the Bitwig API and provides simplified methods for common operations.
 */
public class BitwigApiFacade {
    private final ControllerHost host;
    private final Transport transport;
    private final Logger logger;
    private final CursorDevice cursorDevice;
    private final RemoteControlsPage deviceParameterBank;
    private final TrackBank trackBank;

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

        // Initialize device control - use CursorTrack.createCursorDevice() instead of deprecated host.createCursorDevice()
        CursorTrack cursorTrack = host.createCursorTrack(0, 0);
        this.cursorDevice = cursorTrack.createCursorDevice();
        this.deviceParameterBank = cursorDevice.createCursorRemoteControlsPage(8);

        // Initialize track bank for clip launching (8 tracks, 8 scenes should be sufficient for MVP)
        this.trackBank = host.createTrackBank(8, 0, 8);

        // Mark interest in device properties to enable value access
        cursorDevice.exists().markInterested();
        cursorDevice.name().markInterested();

        // Mark interest in all parameter properties to enable value access
        for (int i = 0; i < 8; i++) {
            RemoteControl parameter = deviceParameterBank.getParameter(i);
            parameter.name().markInterested();
            parameter.value().markInterested();
            parameter.displayedValue().markInterested();
        }

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
     * Returns the name of the track at the given index, or null if not present.
     *
     * @param index the track index
     * @return the track name, or null if not present
     */
    public String getTrackNameByIndex(int index) {
        if (index < 0 || index >= trackBank.getSizeOfBank()) {
            return null;
        }
        Track track = trackBank.getItemAt(index);
        if (track.exists().get()) {
            return track.name().get();
        }
        return null;
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
     * @return The device name or null if no device is selected
     */
    public String getSelectedDeviceName() {
        logger.info("BitwigApiFacade: Getting selected device name");
        if (!isDeviceSelected()) {
            return null;
        }
        return cursorDevice.name().get();
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
     * @throws IllegalArgumentException if parameterIndex is out of range or value is out of range
     * @throws RuntimeException        if no device is selected or Bitwig API error occurs
     */
    public void setSelectedDeviceParameter(int parameterIndex, double value) {
        logger.info("BitwigApiFacade: Setting parameter " + parameterIndex + " to " + value);

        // Validate parameter index
        if (parameterIndex < 0 || parameterIndex > 7) {
            String errorMsg = "Parameter index must be between 0-7, got: " + parameterIndex;
            logger.error("BitwigApiFacade: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // Validate value range
        if (value < 0.0 || value > 1.0) {
            String errorMsg = "Parameter value must be between 0.0-1.0, got: " + value;
            logger.error("BitwigApiFacade: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // Check if device is selected
        if (!isDeviceSelected()) {
            String errorMsg = "No device is currently selected";
            logger.error("BitwigApiFacade: " + errorMsg);
            throw new RuntimeException(errorMsg);
        }

        try {
            RemoteControl parameter = deviceParameterBank.getParameter(parameterIndex);
            parameter.value().set(value);
            logger.info("BitwigApiFacade: Successfully set parameter " + parameterIndex + " to " + value);
        } catch (Exception e) {
            String errorMsg = "Failed to set parameter " + parameterIndex + ": " + e.getMessage();
            logger.error("BitwigApiFacade: " + errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Finds a track by name using case-sensitive matching.
     *
     * @param trackName The name of the track to find
     * @return true if the track is found, false otherwise
     */
    public boolean findTrackByName(String trackName) {
        logger.info("BitwigApiFacade: Searching for track '" + trackName + "'");

        for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
            Track track = trackBank.getItemAt(i);
            if (track.exists().get()) {
                String currentTrackName = track.name().get();
                if (trackName.equals(currentTrackName)) {
                    logger.info("BitwigApiFacade: Found track '" + trackName + "' at index " + i);
                    return true;
                }
            }
        }

        logger.info("BitwigApiFacade: Track '" + trackName + "' not found");
        return false;
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
     * @return true if the clip was launched successfully, false otherwise
     */
    public boolean launchClip(String trackName, int clipIndex) {
        logger.info("BitwigApiFacade: Launching clip at " + trackName + "[" + clipIndex + "]");

        for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
            Track track = trackBank.getItemAt(i);
            if (track.exists().get() && trackName.equals(track.name().get())) {
                try {
                    ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();

                    if (clipIndex >= 0 && clipIndex < slotBank.getSizeOfBank()) {
                        ClipLauncherSlot slot = slotBank.getItemAt(clipIndex);
                        slot.launch();
                        logger.info("BitwigApiFacade: Successfully launched clip at " + trackName + "[" + clipIndex + "]");
                        return true;
                    } else {
                        logger.error("BitwigApiFacade: Clip index " + clipIndex + " out of bounds for track '" + trackName + "'");
                        return false;
                    }
                } catch (Exception e) {
                    logger.error("BitwigApiFacade: Error launching clip at " + trackName + "[" + clipIndex + "]: " + e.getMessage());
                    return false;
                }
            }
        }

        logger.error("BitwigApiFacade: Track '" + trackName + "' not found for clip launch");
        return false;
    }
}
