# Track

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent tracks in Bitwig Studio.

**Extends:** `com.bitwig.extension.controller.api.Channel`

## Methods

### position()

Value that reports the position of the track within the list of Bitwig Studio tracks.

**Returns:** type IntegerValue

### addPositionObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the position of the track within the list of Bitwig Studio tracks.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer parameter

**Returns:** type void

### getClipLauncherSlots()

Returns an object that can be used to access the clip launcher slots of the track.

**Returns:** type ClipLauncherSlotBank - an object that represents the clip launcher slots of the track

### clipLauncherSlotBank()

Returns an object that can be used to access the clip launcher slots of the track.

**Returns:** type ClipLauncherSlotBank - an object that represents the clip launcher slots of the track

### getClipLauncher()

**Returns:** type ClipLauncherSlotBank

### addIsQueuedForStopObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the clip launcher slots are queued for stop.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument.

**Returns:** type void

### getArm()

Returns an object that provides access to the arm state of the track.

**Returns:** type SettableBooleanValue - a boolean value object

### arm()

Returns an object that provides access to the arm state of the track.

**Returns:** type SettableBooleanValue - a boolean value object

### getMonitor()

Returns an object that provides access to the monitoring state of the track.

**Returns:** type SettableBooleanValue - a boolean value object

### monitor()

Returns an object that provides access to the monitoring state of the track.

**Returns:** type SettableBooleanValue - a boolean value object

### isMonitoring()

Returns an object that provides a readout of the monitoring state of the track.

**Returns:** type BooleanValue - a read-only boolean value object

### getAutoMonitor()

Returns an object that provides access to the auto-monitoring state of the track.

**Returns:** type SettableBooleanValue - a boolean value object

### autoMonitor()

Returns an object that provides access to the auto-monitoring state of the track.

**Returns:** type SettableBooleanValue - a boolean value object

### monitorMode()

Returns an object that provides access to the auto-monitoring mode of the track.

**Returns:** type SettableEnumValue - a boolean value object

### getCrossFadeMode()

Returns an object that provides access to the cross-fade mode of the track.

**Returns:** type SettableEnumValue - an enum value object that has three possible states: "A", "B", or "AB"

### crossFadeMode()

Returns an object that provides access to the cross-fade mode of the track.

**Returns:** type SettableEnumValue - an enum value object that has three possible states: "A", "B", or "AB"

### isStopped()

Value that reports if this track is currently stopped. When a track is stopped it is not playing content
 from the arranger or clip launcher.

**Returns:** type BooleanValue

### getIsMatrixStopped()

Returns a value object that provides access to the clip launcher playback state of the track.

**Returns:** type BooleanValue - a boolean value object that indicates if the clip launcher is stopped for this track

### getIsMatrixQueuedForStop()

Returns a value object that provides access to the clip launcher's queue-for-stop state on this track. A
 clip is considered to be queued for stop when playback has been requested to be stopped on that clip,
 but the playback has not stopped yet due to the current launch quantization settings.

**Returns:** type BooleanValue - a boolean value object that indicates if the clip launcher slots have been queued for stop

### isQueuedForStop()

Value that reports if the clip launcher slots are queued for stop.

**Returns:** type BooleanValue

### getSourceSelector()

Returns the source selector for the track, which is shown in the IO section of the track in Bitwig
 Studio and lists either note or audio sources or both depending on the track type.

**Returns:** type SourceSelector - a source selector object

### sourceSelector()

Returns the source selector for the track, which is shown in the IO section of the track in Bitwig
 Studio and lists either note or audio sources or both depending on the track type.

**Returns:** type SourceSelector - a source selector object

### stop()

Stops playback of the track.

**Returns:** type void

### stopAction()

Action to call stop().

**Returns:** type HardwareActionBindable

### stopAlt()

Stops playback of the track using alternative quantization.

**Returns:** type void

### stopAltAction()

Action to call stopAlt().

**Returns:** type HardwareActionBindable

### returnToArrangement()

Calling this method causes the arrangement sequencer to take over playback.

**Returns:** type void

### setName(java.lang.String)

Updates the name of the track.

**Parameters:**
- `name` (type String): the new track name

**Returns:** type void

### addPitchNamesObserver(com.bitwig.extension.callback.IndexedStringValueChangedCallback)

Registers an observer that reports names for note key values on this track. The track might provide
 special names for certain keys if it contains instruments that support that features, such as the Bitwig
 Drum Machine.

**Parameters:**
- `callback` (type IndexedStringValueChangedCallback): a callback function that receives two arguments: 1. the key value in the range [0..127], and
           2. the name string

**Returns:** type void

### playNote(int,int)

Plays a note on the track with a default duration and the given key and velocity.

**Parameters:**
- `key` (type int): the key value of the played note
- `velocity` (type int): the velocity of the played note

**Returns:** type void

### startNote(int,int)

Starts playing a note on the track with the given key and velocity.

**Parameters:**
- `key` (type int): the key value of the played note
- `velocity` (type int): the velocity of the played note

**Returns:** type void

### stopNote(int,int)

Stops playing a currently played note.

**Parameters:**
- `key` (type int): the key value of the playing note
- `velocity` (type int): the note-off velocity

**Returns:** type void

### sendMidi(int,int,int)

Sends a MIDI message to the hardware device.

**Parameters:**
- `status` (type int): the status byte of the MIDI message
- `data1` (type int): the data1 part of the MIDI message
- `data2` (type int): the data2 part of the MIDI message

**Returns:** type void

### trackType()

Value that reports the track type. Possible reported track types are `Group`, `Instrument`, `Audio`,
 `Hybrid`, `Effect` or `Master`.

**Returns:** type StringValue

### addTrackTypeObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the track type. Possible reported track types are `Group`,
 `Instrument`, `Audio`, `Hybrid`, `Effect` or `Master`.

**Parameters:**
- `numChars` (type int): the maximum number of characters used for the reported track type
- `textWhenUnassigned` (type String): the default text that gets reported when the track is not yet associated with a Bitwig Studio
           track.
- `callback` (type StringValueChangedCallback): a callback function that receives a single track type parameter (string).

**Returns:** type void

### isGroup()

Value that reports if the track may contain child tracks, which is the case for group tracks.

**Returns:** type BooleanValue

### isGroupExpanded()

Value that indicates if the group's child tracks are visible.

**Returns:** type SettableBooleanValue

### getIsPreFader()

If the track is an effect track, returns an object that indicates if the effect track is configured
 as pre-fader.

**Returns:** type SettableBooleanValue

### addIsGroupObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the track may contain child tracks, which is the case for group
 tracks.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### getCanHoldNoteData()

Returns an object that indicates if the track may contain notes.

**Returns:** type SettableBooleanValue - a boolean value object

### canHoldNoteData()

Returns an object that indicates if the track may contain notes.

**Returns:** type SettableBooleanValue - a boolean value object

### getCanHoldAudioData()

Returns an object that indicates if the track may contain audio events.

**Returns:** type SettableBooleanValue - a boolean value object

### canHoldAudioData()

Returns an object that indicates if the track may contain audio events.

**Returns:** type SettableBooleanValue - a boolean value object

### createCursorDevice()

Returns an object that provides access to the cursor item of the track's device selection as shown in
 the Bitwig Studio user interface.

**Returns:** type CursorDevice - the requested device selection cursor object

### createCursorDevice(java.lang.String)

Creates a named device selection cursor that is independent from the device selection in the Bitwig
 Studio user interface, assuming the name parameter is not null. When `name` is `null` the result is
 equal to calling createCursorDevice().

**Parameters:**
- `name` (type String): the name of the custom device selection cursor, for example "Primary", or `null` to refer to
           the device selection cursor in the arranger cursor track as shown in the Bitwig Studio user
           interface.

**Returns:** type CursorDevice - the requested device selection cursor object

### createCursorDevice(java.lang.String,int)

Creates a named device selection cursor that is independent from the device selection in the Bitwig
 Studio user interface, assuming the name parameter is not null. When `name` is `null` the result is
 equal to calling createCursorDevice().

**Parameters:**
- `name` (type String): the name of the custom device selection cursor, for example "Primary", or `null` to refer to
           the device selection cursor in the arranger cursor track as shown in the Bitwig Studio user
           interface.
- `numSends` (type int): the number of sends that are simultaneously accessible in nested channels.

**Returns:** type CursorDevice - the requested device selection cursor object

### getPrimaryDevice()

Gets the channels primary device.

**Returns:** type Device - an object that provides access to the channels primary device.

### getPrimaryInstrument()

**Returns:** type Device

### createTrackBank(int,int,int,boolean)

Returns a track bank with the given number of child tracks, sends and scenes. The track bank will only
 have content if the connected track is a group track.

 A track bank can be seen as a fixed-size window onto the list of tracks in the connected track group
 including their sends and scenes, that can be scrolled in order to access different parts of the track
 list. For example a track bank configured for 8 tracks can show track 1-8, 2-9, 3-10 and so on.

 The idea behind the `bank pattern` is that hardware typically is equipped with a fixed amount of channel
 strips or controls, for example consider a mixing console with 8 channels, but Bitwig Studio documents
 contain a dynamic list of tracks, most likely more tracks than the hardware can control simultaneously.
 The track bank returned by this function provides a convenient interface for controlling which tracks
 are currently shown on the hardware.

 Creating a track bank using this method will consider all tracks in the document, including effect
 tracks and the master track. Use createMainTrackBank(int, int, int, boolean) or createEffectTrackBank(int, int, boolean) in case
 you are only interested in tracks of a certain kind.

**Parameters:**
- `numTracks` (type int): the number of child tracks spanned by the track bank
- `numSends` (type int): the number of sends spanned by the track bank
- `numScenes` (type int): the number of scenes spanned by the track bank
- `hasFlatTrackList` (type boolean): specifies whether the track bank should operate on a flat list of all nested child tracks or
           only on the direct child tracks of the connected group track.

**Returns:** type TrackBank - an object for bank-wise navigation of tracks, sends and scenes

### createMainTrackBank(int,int,int,boolean)

Returns a track bank with the given number of child tracks, sends and scenes. Only audio tracks,
 instrument tracks and hybrid tracks are considered. The track bank will only have content if the
 connected track is a group track. For more information about track banks and the `bank pattern` in
 general, see the documentation for createTrackBank(int, int, int, boolean).

**Parameters:**
- `numTracks` (type int): the number of child tracks spanned by the track bank
- `numSends` (type int): the number of sends spanned by the track bank
- `numScenes` (type int): the number of scenes spanned by the track bank
- `hasFlatTrackList` (type boolean): specifies whether the track bank should operate on a flat list of all nested child tracks or
           only on the direct child tracks of the connected group track.

**Returns:** type TrackBank - an object for bank-wise navigation of tracks, sends and scenes

### createEffectTrackBank(int,int,boolean)

Returns a track bank with the given number of child effect tracks and scenes. Only effect tracks are
 considered. The track bank will only have content if the connected track is a group track. For more
 information about track banks and the `bank pattern` in general, see the documentation for
 createTrackBank(int, int, int, boolean).

**Parameters:**
- `numTracks` (type int): the number of child tracks spanned by the track bank
- `numScenes` (type int): the number of scenes spanned by the track bank
- `hasFlatTrackList` (type boolean): specifies whether the track bank should operate on a flat list of all nested child tracks or
           only on the direct child tracks of the connected group track.

**Returns:** type TrackBank - an object for bank-wise navigation of tracks, sends and scenes

### createEffectTrackBank(int,int,int,boolean)

Returns a track bank with the given number of child effect tracks and scenes. Only effect tracks are
 considered. The track bank will only have content if the connected track is a group track. For more
 information about track banks and the `bank pattern` in general, see the documentation for
 createTrackBank(int, int, int, boolean).

**Parameters:**
- `numTracks` (type int): the number of child tracks spanned by the track bank
- `numSends` (type int): the number of sends spanned by the track bank
- `numScenes` (type int): the number of scenes spanned by the track bank
- `hasFlatTrackList` (type boolean): specifies whether the track bank should operate on a flat list of all nested child tracks or
           only on the direct child tracks of the connected group track.

**Returns:** type TrackBank - an object for bank-wise navigation of tracks, sends and scenes

### createMasterTrack(int)

Returns an object that represents the master track of the connected track group. The returned object
 will only have content if the connected track is a group track.

**Parameters:**
- `numScenes` (type int): the number of scenes for bank-wise navigation of the master tracks clip launcher slots.

**Returns:** type MasterTrack - an object representing the master track of the connected track group.

### createSiblingsTrackBank(int,int,int,boolean,boolean)

Returns a bank of sibling tracks with the given number of tracks, sends and scenes. For more information
 about track banks and the `bank pattern` in general, see the documentation for createTrackBank(int, int, int, boolean).

**Parameters:**
- `numTracks` (type int): the number of child tracks spanned by the track bank
- `numSends` (type int): the number of sends spanned by the track bank
- `numScenes` (type int): the number of scenes spanned by the track bank
- `shouldIncludeEffectTracks` (type boolean): specifies whether effect tracks should be included
- `shouldIncludeMasterTrack` (type boolean): specifies whether the master should be included

**Returns:** type TrackBank - an object for bank-wise navigation of sibling tracks

### afterTrackInsertionPoint()

InsertionPoint that can be used to insert after this track.

**Returns:** type InsertionPoint

### beforeTrackInsertionPoint()

InsertionPoint that can be used to insert after this track.

**Returns:** type InsertionPoint

### createParentTrack(int,int)

Creates an object that represent the parent track.

**Parameters:**
- `numSends` (type int)
- `numScenes` (type int)

**Returns:** type Track

### addNoteSource(com.bitwig.extension.controller.api.NoteInput)

Routes the given noteInput directly to the track regardless of monitoring.

**Parameters:**
- `noteInput` (type NoteInput)

**Returns:** type void

### removeNoteSource(com.bitwig.extension.controller.api.NoteInput)

Removes a routing operated by addNoteSource(NoteInput)

**Parameters:**
- `noteInput` (type NoteInput)

**Returns:** type void

### createNewLauncherClip(int,int)

Will create a new empty clip at or after slot index.
 If necessary, a new scene will be created.
 The new clip will be selected.

**Parameters:**
- `slotIndex` (type int): absolute slot index in the track (unrelated to banks)
- `lengthInBeats` (type int)

**Returns:** type void

### createNewLauncherClip(int)

Will create a new empty clip at or after slot index.
 It will use the default clip length.
 If necessary, a new scene will be created.
 The new clip will be selected.

**Parameters:**
- `slotIndex` (type int): absolute slot index in the track (unrelated to banks)

**Returns:** type void

### recordNewLauncherClip(int)

Will start recording a new clip at or after slot index.
 If necessary, a new scene will be created.
 The new clip will be selected.

**Parameters:**
- `slotIndex` (type int): absolute slot index in the track (unrelated to banks)

**Returns:** type void

### selectSlot(int)

Selects the slot at the given index.

**Parameters:**
- `slotIndex` (type int): absolute slot index in the track (unrelated to banks)

**Returns:** type void

### launchLastClipWithOptions(java.lang.String,java.lang.String)

Launches the last clip with the given options:

**Parameters:**
- `quantization` (type String): possible values are "default", "none", "8", "4", "2", "1", "1/2", "1/4", "1/8", "1/16"
- `launchMode` (type String): possible values are: "default", "from_start", "continue_or_from_start",
                   "continue_or_synced", "synced"

**Returns:** type void

### launchLastClipWithOptionsAction(java.lang.String,java.lang.String)

**Parameters:**
- `quantization` (type String)
- `launchMode` (type String)

**Returns:** type HardwareActionBindable

### createCursorRemoteControlsPage(int)

Creates a cursor for the selected remote controls page in the device with the supplied number of
 parameters. This section will follow the current page selection made by the user in the application.

**Parameters:**
- `parameterCount` (type int): The number of parameters the remote controls should contain

**Returns:** type CursorRemoteControlsPage

### createCursorRemoteControlsPage(java.lang.String,int,java.lang.String)

Creates a cursor for a remote controls page in the device with the supplied number of parameters. This
 section will be independent from the current page selected by the user in Bitwig Studio's user
 interface. The supplied filter is an expression that can be used to match pages this section is
 interested in. The expression is matched by looking at the tags added to the pages. If the expression is
 empty then no filtering will occur.

**Parameters:**
- `name` (type String): A name to associate with this section. This will be used to remember manual mappings made by
           the user within this section.
- `parameterCount` (type int): The number of parameters the remote controls should contain
- `filterExpression` (type String): An expression used to match pages that the user can navigate through. For now this can only be
           the name of a single tag the pages should contain (e.g "drawbars", "dyn", "env", "eq",
           "filter", "fx", "lfo", "mixer", "osc", "overview", "perf").

**Returns:** type CursorRemoteControlsPage

### channelId()

Reports the channel UUID.

**Returns:** type StringValue

### channelIndex()

Reports the channel index.

**Returns:** type IntegerValue

### isActivated()

Returns an object that represents the activated state of the channel.

**Returns:** type SettableBooleanValue - an object that provides access to the channels activated state.

### getVolume()

Gets a representation of the channels volume control.

**Returns:** type Parameter - an object that provides access to the channels volume control.

### volume()

Gets a representation of the channels volume control.

**Returns:** type Parameter - an object that provides access to the channels volume control.

### getPan()

Gets a representation of the channels pan control.

**Returns:** type Parameter - an object that provides access to the channels pan control.

### pan()

Gets a representation of the channels pan control.

**Returns:** type Parameter - an object that provides access to the channels pan control.

### getMute()

Gets a representation of the channels mute control.

**Returns:** type SettableBooleanValue - an object that provides access to the channels mute control.

### mute()

Gets a representation of the channels mute control.

**Returns:** type SettableBooleanValue - an object that provides access to the channels mute control.

### getSolo()

Gets a representation of the channels solo control.

**Returns:** type SoloValue - an object that provides access to the channels solo control.

### solo()

Gets a representation of the channels solo control.

**Returns:** type SoloValue - an object that provides access to the channels solo control.

### isMutedBySolo()

True if the current channel is being muted by an other channel with solo on.

**Returns:** type BooleanValue

### addVuMeterObserver(int,int,boolean,com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer for the VU-meter of this track.

**Parameters:**
- `range` (type int): the number of steps to which the reported values should be scaled. For example a range of 128
           would cause the callback to be called with values between 0 and 127.
- `channel` (type int): 0 for left channel, 1 for right channel, -1 for the sum of both
- `peak` (type boolean): when `true` the peak value is reported, otherwise the RMS value
- `callback` (type IntegerValueChangedCallback): a callback function that takes a single numeric argument. The value is in the range
           [0..range-1].

**Returns:** type void

### addNoteObserver(com.bitwig.extension.callback.NotePlaybackCallback)

Registers an observer that reports notes when they are played on the channel.

**Parameters:**
- `callback` (type NotePlaybackCallback): a callback function that receives three parameters: 1. on/off state (boolean), 2. key (int),
           and 3. velocity (float).

**Returns:** type void

### playingNotes()

Returns an array of the playing notes.

**Returns:** type PlayingNoteArrayValue

### addColorObserver(com.bitwig.extension.callback.ColorValueChangedCallback)

Registers an observer that receives notifications about the color of the channel. The callback gets
 called at least once immediately after this function call to report the current color. Additional calls
 are fired each time the color changes.

**Parameters:**
- `callback` (type ColorValueChangedCallback): a callback function that receives three float parameters in the range [0..1]: 1. red, 2.
           green, and 3. blue.

**Returns:** type void

### color()

Get the color of the channel.

**Returns:** type SettableColorValue

### sendBank()

Gets a SendBank that can be used to navigate the sends of this channel.

**Returns:** type SendBank

### getSend(int)

Gets a representation of the channels send control at the given index.

**Parameters:**
- `index` (type int): the index of the send, must be valid

**Returns:** type Send - an object that provides access to the requested send control.

### duplicate()

Duplicates the track.

**Returns:** type void

### selectInMixer()

Selects the device chain in the Bitwig Studio mixer, in case it is a selectable object.

**Returns:** type void

### addIsSelectedInMixerObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device chain is selected in Bitwig Studio mixer.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter.

**Returns:** type void

### makeVisibleInArranger()

Tries to scroll the contents of the arrangement editor so that the channel becomes visible.

**Returns:** type void

### makeVisibleInMixer()

Tries to scroll the contents of the mixer panel so that the channel becomes visible.

**Returns:** type void

### selectInEditor()

Selects the device chain in Bitwig Studio, in case it is a selectable object.

**Returns:** type void

### name()

Value that reports the name of the device chain, such as the track name or the drum pad
 name.

**Returns:** type SettableStringValue

### addNameObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the name of the device chain, such as the track name or the drum pad
 name.

**Parameters:**
- `numChars` (type int): the maximum number of characters used for the reported name
- `textWhenUnassigned` (type String): the default text that gets reported when the device chain is not associated with an object in
           Bitwig Studio yet.
- `callback` (type StringValueChangedCallback): a callback function that receives a single name parameter (string).

**Returns:** type void

### addIsSelectedInEditorObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device chain is selected in Bitwig Studio editors.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter.

**Returns:** type void

### addIsSelectedObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

**Parameters:**
- `callback` (type BooleanValueChangedCallback)

**Returns:** type void

### createDeviceBank(int)

Returns an object that provides bank-wise navigation of devices.

**Parameters:**
- `numDevices` (type int): the number of devices should be accessible simultaneously

**Returns:** type DeviceBank - the requested device bank object

### createDeviceBrowser(int,int)

Returns an object used for browsing devices, presets and other content. Committing the browsing session
 will load or create a device from the selected resource and insert it into the device chain.

**Parameters:**
- `numFilterColumnEntries` (type int): the size of the window used to navigate the filter column entries.
- `numResultsColumnEntries` (type int): the size of the window used to navigate the results column entries.

**Returns:** type Browser - the requested device browser object.

### select()

**Returns:** type void

### browseToInsertAtStartOfChain()

Starts browsing for content that can be inserted at the start of this device chain.

**Returns:** type void

### browseToInsertAtEndOfChain()

Starts browsing for content that can be inserted at the end of this device chain.

**Returns:** type void

### startOfDeviceChainInsertionPoint()

InsertionPoint that can be used to insert at the start of the device chain.

**Returns:** type InsertionPoint

### endOfDeviceChainInsertionPoint()

InsertionPoint that can be used to insert at the end of the device chain.

**Returns:** type InsertionPoint

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

### deleteObject()

Deletes this object from the document.

 If you want to delete multiple objects at once, see Host.deleteObjects().

**Returns:** type void

### deleteObjectAction()

Deletes this object from the document.

**Returns:** type HardwareActionBindable

### duplicateObject()

Duplicates this object into the document.

 If you want to duplicate multiple objects at once, see Host.duplicateObjects().

**Returns:** type void

### duplicateObjectAction()

Duplicates this object into the document.

**Returns:** type HardwareActionBindable

