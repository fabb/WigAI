# Mixer

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

An interface used to access various commands that can be performed on the Bitwig Studio mixer panel.

 To get an instance of the mixer interface call ControllerHost.createMixer().

## Methods

### isMeterSectionVisible()

Gets an object that allows to show/hide the meter section of the mixer panel. Observers can be
 registered on the returned object for receiving notifications when the meter section switches between
 shown and hidden state.

**Returns:** type SettableBooleanValue - a boolean value object that represents the meter section visibility

### isIoSectionVisible()

Gets an object that allows to show/hide the io section of the mixer panel. Observers can be registered
 on the returned object for receiving notifications when the io section switches between shown and hidden
 state.

**Returns:** type SettableBooleanValue - a boolean value object that represents the io section visibility

### isSendSectionVisible()

Gets an object that allows to show/hide the sends section of the mixer panel. Observers can be
 registered on the returned object for receiving notifications when the sends section switches between
 shown and hidden state.

**Returns:** type SettableBooleanValue - a boolean value object that represents the sends section visibility

### isClipLauncherSectionVisible()

Gets an object that allows to show/hide the clip launcher section of the mixer panel. Observers can be
 registered on the returned object for receiving notifications when the clip launcher section switches
 between shown and hidden state.

**Returns:** type SettableBooleanValue - a boolean value object that represents the clip launcher section visibility

### isDeviceSectionVisible()

Gets an object that allows to show/hide the devices section of the mixer panel. Observers can be
 registered on the returned object for receiving notifications when the devices section switches between
 shown and hidden state.

**Returns:** type SettableBooleanValue - a boolean value object that represents the devices section visibility

### isCrossFadeSectionVisible()

Gets an object that allows to show/hide the cross-fade section of the mixer panel. Observers can be
 registered on the returned object for receiving notifications when the cross-fade section switches
 between shown and hidden state.

**Returns:** type SettableBooleanValue - a boolean value object that represents the cross-fade section visibility

### zoomInTrackWidthsAllAction()

Zooms in all mixer tracks, if it the mixer is visible.

**Returns:** type HardwareActionBindable

### zoomInTrackWidthsAll()

**Returns:** type void

### zoomOutTrackWidthsAllAction()

Zooms out all mixer tracks, if it the mixer is visible.

**Returns:** type HardwareActionBindable

### zoomOutTrackWidthsAll()

**Returns:** type void

### zoomTrackWidthsAllStepper()

Same as zoomInTrackWidthsAllAction/zoomOutTrackWidthsAllAction, but as a stepper

**Returns:** type RelativeHardwarControlBindable

### zoomInTrackWidthsSelectedAction()

Zooms in selected mixer tracks, if it the mixer is visible.

**Returns:** type HardwareActionBindable

### zoomInTrackWidthsSelected()

**Returns:** type void

### zoomOutTrackWidthsSelectedAction()

Zooms out selected mixer tracks, if it the mixer is visible.

**Returns:** type HardwareActionBindable

### zoomOutTrackWidthsSelected()

**Returns:** type void

### zoomTrackWidthsSelectedStepper()

Same as zoomInTrackWidthsSelectedAction/zoomOutTrackWidthsSelectedAction, but as a stepper

**Returns:** type RelativeHardwarControlBindable

### addMeterSectionVisibilityObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the meter section is visible (callback argument is `true`) in the
 mixer panel or not (callback argument is `false`).

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### addIoSectionVisibilityObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the IO section is visible (callback argument is `true`) in the
 mixer panel or not (callback argument is `false`).

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### addSendsSectionVisibilityObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the send control section is visible (callback argument is `true`)
 in the mixer panel or not (callback argument is `false`).

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### addClipLauncherSectionVisibilityObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the clip launcher section is visible (callback argument is `true`)
 in the mixer panel or not (callback argument is `false`).

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### addDeviceSectionVisibilityObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device section is visible (callback argument is `true`) in the
 mixer panel or not (callback argument is `false`).

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### addCrossFadeSectionVisibilityObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the cross-fade section is visible (callback argument is `true`) in
 the mixer panel or not (callback argument is `false`).

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### toggleMeterSectionVisibility()

Toggles the visibility of the meter section in the mixer panel.

**Returns:** type void

### toggleIoSectionVisibility()

Toggles the visibility of the IO section in the mixer panel.

**Returns:** type void

### toggleSendsSectionVisibility()

Toggles the visibility of the send control section in the mixer panel.

**Returns:** type void

### toggleClipLauncherSectionVisibility()

Toggles the visibility of the clip launcher section in the mixer panel.

**Returns:** type void

### toggleDeviceSectionVisibility()

Toggles the visibility of the device section in the mixer panel.

**Returns:** type void

### toggleCrossFadeSectionVisibility()

Toggles the visibility of the cross-fade section in the mixer panel.

**Returns:** type void

