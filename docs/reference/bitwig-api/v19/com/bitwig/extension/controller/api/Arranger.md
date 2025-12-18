# Arranger

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

An interface representing various commands which can be performed on the Bitwig Studio arranger.

 To receive an instance of the application interface call ControllerHost.createArranger().

**Extends:** `com.bitwig.extension.controller.api.TimelineEditor`

## Methods

### isPlaybackFollowEnabled()

Gets an object that allows to enable/disable arranger playback follow. Observers can be registered on
 the returned object for receiving notifications when the setting switches between on and off.

**Returns:** type SettableBooleanValue - a boolean value object that represents the enabled state of arranger playback follow

### hasDoubleRowTrackHeight()

Gets an object that allows to control the arranger track height. Observers can be registered on the
 returned object for receiving notifications when the track height changes.

**Returns:** type SettableBooleanValue - a boolean value object that has the state `true` when the tracks have double row height and
         `false` when the tracks have single row height.

### areCueMarkersVisible()

Gets an object that allows to show/hide the cue markers in the arranger panel. Observers can be
 registered on the returned object for receiving notifications when the cue marker lane switches between
 shown and hidden.

**Returns:** type SettableBooleanValue - a boolean value object that represents the cue marker section visibility

### isClipLauncherVisible()

Gets an object that allows to show/hide the clip launcher in the arranger panel. Observers can be
 registered on the returned object for receiving notifications when the clip launcher switches between
 shown and hidden.

**Returns:** type SettableBooleanValue - a boolean value object that represents the clip launcher visibility

### isTimelineVisible()

Gets an object that allows to show/hide the timeline in the arranger panel. Observers can be registered
 on the returned object for receiving notifications when the timeline switches between shown and hidden.

**Returns:** type SettableBooleanValue - a boolean value object that represents the timeline visibility

### isIoSectionVisible()

Gets an object that allows to show/hide the track input/output choosers in the arranger panel. Observers
 can be registered on the returned object for receiving notifications when the I/O section switches
 between shown and hidden.

**Returns:** type SettableBooleanValue - a boolean value object that represents the visibility of the track I/O section

### areEffectTracksVisible()

Gets an object that allows to show/hide the effect tracks in the arranger panel. Observers can be
 registered on the returned object for receiving notifications when the effect track section switches
 between shown and hidden.

**Returns:** type SettableBooleanValue - a boolean value object that represents the visibility of the effect track section

### createCueMarkerBank(int)

Returns an object that provides access to a bank of successive cue markers using a window configured with
 the given size, that can be scrolled over the list of markers.

**Parameters:**
- `size` (type int): the number of simultaneously accessible items

**Returns:** type CueMarkerBank - the requested item bank object

### zoomInLaneHeightsAllAction()

Zooms in all arranger lanes, if it the arranger is visible.

**Returns:** type HardwareActionBindable

### zoomInLaneHeightsAll()

**Returns:** type void

### zoomOutLaneHeightsAllAction()

Zooms out all arranger lanes, if it the arranger is visible.

**Returns:** type HardwareActionBindable

### zoomOutLaneHeightsAll()

**Returns:** type void

### zoomLaneHeightsAllStepper()

Same as zoomInLaneHeightsAllAction/zoomOutLaneHeightsAllAction, but as a stepper

**Returns:** type RelativeHardwarControlBindable

### zoomInLaneHeightsSelectedAction()

Zooms in selected arranger lanes, if it the arranger is visible.

**Returns:** type HardwareActionBindable

### zoomInLaneHeightsSelected()

**Returns:** type void

### zoomOutLaneHeightsSelectedAction()

Zooms out selected arranger lanes, if it the arranger is visible.

**Returns:** type HardwareActionBindable

### zoomOutLaneHeightsSelected()

**Returns:** type void

### zoomLaneHeightsSelectedStepper()

Same as zoomInLaneHeightsSelectedAction/zoomOutLaneHeightsSelectedAction, but as a stepper

**Returns:** type RelativeHardwarControlBindable

### addPlaybackFollowObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if playback-follow is enabled.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function object that accepts a single bool parameter

**Returns:** type void

### addTrackRowHeightObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports the current configuration of the arranger track row height.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function object that accepts a single bool parameter. The parameter indicates if
           the row height is double (`true`) or single (`false`).

**Returns:** type void

### addCueMarkerVisibilityObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the cue marker lane is visible.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function object that accepts a single bool parameter.

**Returns:** type void

### togglePlaybackFollow()

Toggles the playback follow state.

**Returns:** type void

### toggleTrackRowHeight()

Toggles the arranger track row height between `double` and `single`.

**Returns:** type void

### toggleCueMarkerVisibility()

Toggles the visibility of the arranger cue marker lane.

**Returns:** type void

### zoomInAction()

Zooms in the timeline, if the timeline editor is visible.

**Returns:** type HardwareActionBindable

### zoomIn()

**Returns:** type void

### zoomOutAction()

Zooms out the timeline, if the timeline editor is visible.

**Returns:** type HardwareActionBindable

### zoomOut()

**Returns:** type void

### zoomLevel()

Smoothly adjusts the zoom level

**Returns:** type RelativeHardwarControlBindable

### zoomToFitAction()

Adjusts the zoom level of the timeline so that all content becomes visible, if the timeline editor is visible.

**Returns:** type HardwareActionBindable

### zoomToFit()

**Returns:** type void

### zoomToSelectionAction()

Adjusts the zoom level of the timeline so that it matches the active selection, if the timeline editor is visible.

**Returns:** type HardwareActionBindable

### zoomToSelection()

**Returns:** type void

### zoomToFitSelectionOrAllAction()

Toggles the timeline between zoomToSelection and zoomToFit, if it is visible.

**Returns:** type HardwareActionBindable

### zoomToFitSelectionOrAll()

**Returns:** type void

### zoomToFitSelectionOrPreviousAction()

Toggles the timeline between zoomToSelection and the last śet zoom level, if it is visible.

**Returns:** type HardwareActionBindable

### zoomToFitSelectionOrPrevious()

**Returns:** type void

### getHorizontalScrollbarModel()

Get the horizontal (time) scrollbar model.

**Returns:** type ScrollbarModel

