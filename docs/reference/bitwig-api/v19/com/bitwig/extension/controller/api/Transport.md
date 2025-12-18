# Transport

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

An interface representing the transport section in Bitwig Studio.

**Extends:** `com.bitwig.extension.controller.api.ObjectProxy`

## Methods

### play()

Starts playback in the Bitwig Studio transport.

**Returns:** type void

### continuePlayback()

Continues the playback in the Bitwig Studio transport.

**Returns:** type void

### playAction()

Action that can be used to play the transport.

**Returns:** type HardwareActionBindable

### continuePlaybackAction()

Action that can be used to continue the transport.

**Returns:** type HardwareActionBindable

### stop()

Stops playback in the Bitwig Studio transport.

**Returns:** type void

### stopAction()

Action that can be used to stop the transport.

**Returns:** type HardwareActionBindable

### togglePlay()

Toggles the transport playback state between playing and stopped.

**Returns:** type void

### restart()

When the transport is stopped, calling this function starts transport playback, otherwise the transport
 is first stopped and the playback is restarted from the last play-start position.

**Returns:** type void

### restartAction()

Action that can be used to restart the transport.

**Returns:** type HardwareActionBindable

### record()

Starts recording in the Bitwig Studio transport.

**Returns:** type void

### recordAction()

Action that can be used to start recording

**Returns:** type HardwareActionBindable

### rewind()

Rewinds the Bitwig Studio transport to the beginning of the arrangement.

**Returns:** type void

### rewindAction()

Action that can be used to rewind the transport.

**Returns:** type HardwareActionBindable

### fastForward()

Calling this function is equivalent to pressing the fast forward button in the Bitwig Studio transport.

**Returns:** type void

### fastForwardAction()

Action that can be used to fast forward the transport.

**Returns:** type HardwareActionBindable

### tapTempo()

When calling this function multiple times, the timing of those calls gets evaluated and causes
 adjustments to the project tempo.

**Returns:** type void

### tapTempoAction()

Action that can be used to tap the tempo.

**Returns:** type HardwareActionBindable

### isPlaying()

Value that reports if the Bitwig Studio transport is playing.

**Returns:** type SettableBooleanValue

### addIsPlayingObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the Bitwig Studio transport is playing.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` if playing, `false`
           otherwise).

**Returns:** type void

### isArrangerRecordEnabled()

Value that reports if the Bitwig Studio transport is recording.

**Returns:** type SettableBooleanValue

### addIsRecordingObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the Bitwig Studio transport is recording.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` if recording, `false`
           otherwise).

**Returns:** type void

### isArrangerOverdubEnabled()

Value that reports if overdubbing is enabled in Bitwig Studio.

**Returns:** type SettableBooleanValue

### addOverdubObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if over-dubbing is enabled in Bitwig Studio.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` if over-dubbing is
           enabled, `false` otherwise).

**Returns:** type void

### isClipLauncherOverdubEnabled()

Value reports if clip launcher overdubbing is enabled in Bitwig Studio.

**Returns:** type SettableBooleanValue

### addLauncherOverdubObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if clip launcher over-dubbing is enabled in Bitwig Studio.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` if clip launcher
           over-dubbing is enabled, `false` otherwise).

**Returns:** type void

### automationWriteMode()

Value that reports the current automation write mode. Possible values are `"latch"`, `"touch"` or
 `"write"`.

**Returns:** type SettableEnumValue

### addAutomationWriteModeObserver(com.bitwig.extension.callback.EnumValueChangedCallback)

Registers an observer that reports the current automation write mode.

**Parameters:**
- `callback` (type EnumValueChangedCallback): a callback function that receives a single string argument. Possible values are `"latch"`,
           `"touch"` or `"write"`.

**Returns:** type void

### isArrangerAutomationWriteEnabled()

Value that reports if automation write is currently enabled for the arranger.

**Returns:** type SettableBooleanValue

### addIsWritingArrangerAutomationObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if Bitwig Studio is currently writing arranger automation.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` when arranger automation
           write is enabled, `false` otherwise).

**Returns:** type void

### isClipLauncherAutomationWriteEnabled()

Value that reports if automation write is currently enabled on the clip launcher.

**Returns:** type SettableBooleanValue

### addIsWritingClipLauncherAutomationObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if Bitwig Studio is currently writing clip launcher automation.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` when clip launcher
           automation write is enabled, `false` otherwise).

**Returns:** type void

### isAutomationOverrideActive()

Value that indicates if automation override is currently on.

**Returns:** type BooleanValue

### addAutomationOverrideObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if automation is overridden in Bitwig Studio.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` if overridden, `false`
           otherwise).

**Returns:** type void

### isArrangerLoopEnabled()

Value that indicates if the loop is currently active or not.

**Returns:** type SettableBooleanValue

### arrangerLoopStart()

Value that corresponds to the start time of the arranger loop

**Returns:** type SettableBeatTimeValue

### arrangerLoopDuration()

Value that corresponds to the duration of the arranger loop

**Returns:** type SettableBeatTimeValue

### addIsLoopActiveObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if arranger looping is enabled in Bitwig Studio.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` when enabled, `false`
           otherwise).

**Returns:** type void

### isPunchInEnabled()

Value that reports if punch-in is enabled in the Bitwig Studio transport.

**Returns:** type SettableBooleanValue

### addPunchInObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if punch-in is enabled in the Bitwig Studio transport.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` when punch-in is enabled,
           `false` otherwise).

**Returns:** type void

### isPunchOutEnabled()

Value that reports if punch-in is enabled in the Bitwig Studio transport.

**Returns:** type SettableBooleanValue

### addPunchOutObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if punch-out is enabled in the Bitwig Studio transport.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` when punch-out is enabled,
           `false` otherwise).

**Returns:** type void

### isMetronomeEnabled()

Value that reports if the metronome is enabled in Bitwig Studio.

**Returns:** type SettableBooleanValue

### addClickObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the metronome is enabled in Bitwig Studio.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` when the metronome is
           enabled, `false` otherwise).

**Returns:** type void

### isMetronomeTickPlaybackEnabled()

Value that reports if the metronome has tick playback enabled.

**Returns:** type SettableBooleanValue

### addMetronomeTicksObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the metronome has tick playback enabled.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument (`true` if metronome ticks, are
           enabled, `false` otherwise).

**Returns:** type void

### metronomeVolume()

Value that reports the metronome volume.

**Returns:** type SettableRangedValue

### addMetronomeVolumeObserver(com.bitwig.extension.callback.DoubleValueChangedCallback)

Registers an observer that reports the metronome volume.

**Parameters:**
- `callback` (type DoubleValueChangedCallback): a callback function that receives a single numeric argument.

**Returns:** type void

### isMetronomeAudibleDuringPreRoll()

Value that reports if the metronome is audible during pre-roll.

**Returns:** type SettableBooleanValue

### addPreRollClickObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the metronome is audible during pre-roll.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument.

**Returns:** type void

### preRoll()

Value that reports the current pre-roll setting. Possible values are `"none"`, `"one_bar"`,
 `"two_bars"`, or `"four_bars"`.

**Returns:** type SettableEnumValue

### addPreRollObserver(com.bitwig.extension.callback.EnumValueChangedCallback)

Registers an observer that reports the current pre-roll setting.

**Parameters:**
- `callback` (type EnumValueChangedCallback): a callback function that receives a single string argument. Possible values are `"none"`,
           `"one_bar"`, `"two_bars"`, or `"four_bars"`.

**Returns:** type void

### toggleLoop()

Toggles the enabled state of the arranger loop in Bitwig Studio.

**Returns:** type void

### setLoop(boolean)

Enables of disables the arranger loop according to the given parameter.

**Parameters:**
- `isEnabled` (type boolean): `true` to enable the arranger loop, `false` otherwise

**Returns:** type void

### togglePunchIn()

Toggles the punch-in enabled state of the Bitwig Studio transport.

**Returns:** type void

### togglePunchOut()

Toggles the punch-out enabled state of the Bitwig Studio transport.

**Returns:** type void

### toggleClick()

Toggles the metronome enabled state of the Bitwig Studio transport.

**Returns:** type void

### setClick(boolean)

Enables of disables the metronome according to the given parameter.

**Parameters:**
- `isEnabled` (type boolean): `true` to enable the metronome, `false` otherwise

**Returns:** type void

### toggleMetronomeTicks()

Toggles the enabled state of the metronome ticks.

**Returns:** type void

### toggleMetronomeDuringPreRoll()

Toggles the enabled state of the metronome during pre-roll.

**Returns:** type void

### setPreRoll(java.lang.String)

Updates the transport pre-roll setting according to the given parameter.

**Parameters:**
- `value` (type String): the new pre-roll setting, either `"none"`, `"one_bar"`, `"two_bars"`, or `"four_bars"`.

**Returns:** type void

### setMetronomeValue(java.lang.Number,java.lang.Number)

Sets the metronome volume.

**Parameters:**
- `amount` (type Number): the new metronome volume relative to the specified range. Values should be in the range
           [0..range-1].
- `range` (type Number): the range of the provided amount value

**Returns:** type void

### toggleOverdub()

Toggles the over-dubbing enabled state of the Bitwig Studio transport.

**Returns:** type void

### setOverdub(boolean)

Enables of disables arranger over-dubbing according to the given parameter.

**Parameters:**
- `isEnabled` (type boolean): `true` to enable over-dubbing, `false` otherwise

**Returns:** type void

### toggleLauncherOverdub()

Toggles clip launcher overdubbing in Bitwig Studio.

**Returns:** type void

### setLauncherOverdub(boolean)

Enables of disables clip launcher over-dubbing according to the given parameter.

**Parameters:**
- `isEnabled` (type boolean): `true` to enable the over-dubbing, `false` otherwise

**Returns:** type void

### setAutomationWriteMode(java.lang.String)

Sets the automation write mode.

**Parameters:**
- `mode` (type String): the string that identifies the new automation write mode. Possible values are `"latch"`,
           `"touch"` or `"write"`.

**Returns:** type void

### toggleLatchAutomationWriteMode()

Toggles the latch automation write mode in the Bitwig Studio transport.

**Returns:** type void

### toggleWriteArrangerAutomation()

Toggles the arranger automation write enabled state of the Bitwig Studio transport.

**Returns:** type void

### toggleWriteClipLauncherAutomation()

Toggles the clip launcher automation write enabled state of the Bitwig Studio transport.

**Returns:** type void

### resetAutomationOverrides()

Resets any automation overrides in Bitwig Studio.

**Returns:** type void

### returnToArrangement()

Switches playback to the arrangement sequencer on all tracks.

**Returns:** type void

### getTempo()

Returns an object that provides access to the project tempo.

**Returns:** type Parameter - the requested tempo value object

### tempo()

Returns an object that provides access to the project tempo.

**Returns:** type Parameter - the requested tempo value object

### increaseTempo(java.lang.Number,java.lang.Number)

Increases the project tempo value by the given amount, which is specified relative to the given range.

**Parameters:**
- `amount` (type Number): the new tempo value relative to the specified range. Values should be in the range
           [0..range-1].
- `range` (type Number): the range of the provided amount value

**Returns:** type void

### getPosition()

Returns an object that provides access to the transport position in Bitwig Studio.

**Returns:** type SettableBeatTimeValue - a beat time object that represents the transport position

### playPosition()

Returns an object that provides access to the current transport position.

**Returns:** type BeatTimeValue - beat-time value

### playPositionInSeconds()

Returns an object that provides access to the current transport position in seconds.

**Returns:** type DoubleValue - value (seconds)

### playStartPosition()

Returns an object that provides access to the transport's play-start position. (blue triangle)

**Returns:** type SettableBeatTimeValue - beat-time value

### playStartPositionInSeconds()

Returns an object that provides access to the transport's play-start position in seconds. (blue triangle)

**Returns:** type SettableDoubleValue - value (seconds)

### launchFromPlayStartPosition()

Make the transport jump to the play-start position.

**Returns:** type void

### launchFromPlayStartPositionAction()

**Returns:** type HardwareActionBindable

### jumpToPlayStartPosition()

Make the transport jump to the play-start position.

**Returns:** type void

### jumpToPlayStartPositionAction()

**Returns:** type HardwareActionBindable

### jumpToPreviousCueMarker()

Make the transport jump to the previous cue marker.

**Returns:** type void

### jumpToPreviousCueMarkerAction()

**Returns:** type HardwareActionBindable

### jumpToNextCueMarker()

Make the transport jump to the previous cue marker.

**Returns:** type void

### jumpToNextCueMarkerAction()

**Returns:** type HardwareActionBindable

### setPosition(double)

Sets the transport playback position to the given beat time value.

**Parameters:**
- `beats` (type double): the new playback position in beats

**Returns:** type void

### incPosition(double,boolean)

Increases the transport position value by the given number of beats, which is specified relative to the
 given range.

**Parameters:**
- `beats` (type double): the beat time value that gets added to the current transport position. Values have double
           precision and can be positive or negative.
- `snap` (type boolean): when `true` the actual new transport position will be quantized to the beat grid, when `false`
           the position will be increased exactly by the specified beat time

**Returns:** type void

### getInPosition()

Returns an object that provides access to the punch-in position in the Bitwig Studio transport.

**Returns:** type SettableBeatTimeValue - a beat time object that represents the punch-in position

### getOutPosition()

Returns an object that provides access to the punch-out position in the Bitwig Studio transport.

**Returns:** type SettableBeatTimeValue - a beat time object that represents the punch-out position

### addCueMarkerAtPlaybackPosition()

Adds a cue marker at the current position

**Returns:** type void

### addCueMarkerAtPlaybackPositionAction()

**Returns:** type HardwareActionBindable

### getCrossfade()

Returns an object that provides access to the cross-fader, used for mixing between A/B-channels as
 specified on the Bitwig Studio tracks.

**Returns:** type Parameter

### crossfade()

Returns an object that provides access to the cross-fader, used for mixing between A/B-channels as
 specified on the Bitwig Studio tracks.

**Returns:** type Parameter

### getTimeSignature()

Returns an object that provides access to the transport time signature.

**Returns:** type TimeSignatureValue - the time signature value object that represents the transport time signature.

### timeSignature()

Returns an object that provides access to the transport time signature.

**Returns:** type TimeSignatureValue - the time signature value object that represents the transport time signature.

### clipLauncherPostRecordingAction()

Value that reports the current clip launcher post recording action. Possible values are `"off"`,
 `"play_recorded"`, `"record_next_free_slot"`, `"stop"`, `"return_to_arrangement"`,
 `"return_to_previous_clip"` or `"play_random"`.

**Returns:** type SettableEnumValue

### addClipLauncherPostRecordingActionObserver(com.bitwig.extension.callback.EnumValueChangedCallback)

Registers an observer that reports the current clip launcher post recording action.

**Parameters:**
- `callback` (type EnumValueChangedCallback): a callback function that receives a single string argument. Possible values are `"off"`,
           `"play_recorded"`, `"record_next_free_slot"`, `"stop"`, `"return_to_arrangement"`,
           `"return_to_previous_clip"` or `"play_random"`.

**Returns:** type void

### setClipLauncherPostRecordingAction(java.lang.String)

Sets the automation write mode.

**Parameters:**
- `action` (type String): the string that identifies the new automation write mode. Possible values are `"off"`,
           `"play_recorded"`, `"record_next_free_slot"`, `"stop"`, `"return_to_arrangement"`,
           `"return_to_previous_clip"` or `"play_random"`.

**Returns:** type void

### getClipLauncherPostRecordingTimeOffset()

Returns an object that provides access to the clip launcher post recording time offset.

**Returns:** type SettableBeatTimeValue - a beat time object that represents the post recording time offset

### defaultLaunchQuantization()

Setting for the default launch quantization.

 Possible values are `"none"`, `"8"`, `"4"`, `"2"`, `"1"`, `"1/2"`, `"1/4"`, `"1/8"`, `"1/16"`.

**Returns:** type SettableEnumValue

### isFillModeActive()

Value that indicates if the project's fill mode is active or not.

**Returns:** type SettableBooleanValue

### exists()

Returns a value object that indicates if the object being proxied exists, or if it has content.

**Returns:** type BooleanValue

### createEqualsValue(com.bitwig.extension.controller.api.ObjectProxy)

Creates a BooleanValue that determines this proxy is considered equal to another proxy. For this
 to be the case both proxies need to be proxying the same target object.

**Parameters:**
- `other` (type ObjectProxy)

**Returns:** type BooleanValue

### isSubscribed()

Determines if this object is currently 'subscribed'. In the subscribed state it will notify any
 observers registered on it.

**Returns:** type boolean

### setIsSubscribed(boolean)

Sets whether the driver currently considers this object 'active' or not.

**Parameters:**
- `value` (type boolean)

**Returns:** type void

### subscribe()

Subscribes the driver to this object.

**Returns:** type void

### unsubscribe()

Unsubscribes the driver from this object.

**Returns:** type void

