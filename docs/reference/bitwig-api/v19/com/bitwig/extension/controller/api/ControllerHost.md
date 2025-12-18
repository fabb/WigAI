# ControllerHost

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

An interface representing the host application to the script. A singleton instance of this interface is
 available in the global scope of each script. The methods provided by this interface can be divided in
 different categories:

 1. functions for registering the script in Bitwig Studio, so that it can be listed, detected and configured
 in the controller preferences. The methods that belong to this group are defineController(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String),
 defineMidiPorts(int, int), defineSysexIdentityReply(java.lang.String) and addDeviceNameBasedDiscoveryPair(java.lang.String[], java.lang.String[]).
 2. functions for creating objects that provide access to the various areas of Bitwig Studio to the script.
 The name of those methods typically start with `create...` 3. functions for printing to the Control Surface
 Console, which can be opened from the `View` menu of Bitwig Studio. 4. functions for determining the name
 of the host application, API version, the host operating system and such.

 The first group of methods should be called on the global scope of the script. The function in the second
 and third group are typically called from the init method of the script or other handler functions. The
 last group is probably only required in rare cases and can be called any time.

**Extends:** `com.bitwig.extension.api.Host`

## Methods

### restart()

Restarts this controller.

**Returns:** type void

### loadAPI(int)

Loads the supplied API version into the calling script. This is only intended to be called from a
 controller script. It cannot be called from a Java controller extension.

**Parameters:**
- `version` (type int)

**Returns:** type void

### useBetaApi()

Call this method to allow your script to use Beta APIs.

 Beta APIs are still on development and might not be available in a future version of Bitwig Studio.

 Turning this flag to true, will flag your extension as being a beta extension which might not work after
 updating Bitwig Studio.

**Returns:** type void

### shouldFailOnDeprecatedUse()

Determines whether the calling script should fail if it calls a deprecated method based on the API
 version that it requested.

**Returns:** type boolean

### setShouldFailOnDeprecatedUse(boolean)

Sets whether the calling script should fail if it calls a deprecated method based on the API version
 that it requested. This is only intended to be called from a controller script. It cannot be called from
 a Java controller extension.

**Parameters:**
- `value` (type boolean)

**Returns:** type void

### load(java.lang.String)

Loads the script defined by the supplied path. This is only intended to be called from a controller
 script. It cannot be called from a Java controller extension.

**Parameters:**
- `path` (type String)

**Returns:** type void

### platformIsWindows()

Indicates if the host platform is Windows.

**Returns:** type boolean - `true` if the host platform is Windows, `false` otherwise.

### platformIsMac()

Indicates if the host platform is Apple Mac OS X.

**Returns:** type boolean - `true` if the host platform is Mac, `false` otherwise.

### platformIsLinux()

Indicates if the host platform is Linux.

**Returns:** type boolean - `true` if the host platform is Linux, `false` otherwise.

### defineController(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)

Registers a controller script with the given parameters. This function must be called once at the global
 scope of the script.

**Parameters:**
- `vendor` (type String): the name of the hardware vendor. Must not be
- `name` (type String): the name of the controller script as listed in the user interface of Bitwig Studio. Must not
           be
- `version` (type String): the version of the controller script. Must not be
- `uuid` (type String): a universal unique identifier (UUID) string that is used to distinguish one script from
           another, for example `550e8400-e29b-11d4-a716-446655440000`. Must not be
- `author` (type String): the name of the script author

**Returns:** type void

### defineMidiPorts(int,int)

Defines the number of MIDI ports for input and output that the device uses. This method should be called
 once in the global scope if the script is supposed to exchange MIDI messages with the device, or if the
 script adds entries to the MIDI input/output choosers in Bitwig Studio. After calling this method the
 individual port objects can be accessed using getMidiInPort(int index) and
 getMidiInPort(int index).

**Parameters:**
- `numInports` (type int): the number of input ports
- `numOutports` (type int): the number of output ports

**Returns:** type void

### getMidiInPort(int)

Returns the MIDI input port with the given index.

**Parameters:**
- `index` (type int): the index of the MIDI input port, must be valid.

**Returns:** type MidiIn - the requested MIDI input port

### getMidiOutPort(int)

Returns the MIDI output port with the given index.

**Parameters:**
- `index` (type int): the index of the MIDI output port, must be valid.

**Returns:** type MidiOut - the requested MIDI output port

### hardwareDevice(int)

Gets the HardwareDevice at the specified index. This index corresponds to the index of the
 HardwareDeviceMatcher specified in the
 ControllerExtensionDefinition.listHardwareDevices(com.bitwig.extension.controller.HardwareDeviceMatcherList)

**Parameters:**
- `index` (type int)

**Returns:** type HardwareDevice

### addDeviceNameBasedDiscoveryPair(java.lang.String[],java.lang.String[])

Registers patterns which are used to automatically detect hardware devices that can be used with the
 script.

 When the user clicks on the `detect` button in the Bitwig Studio controller preferences dialog, Bitwig
 Studio searches for connected controller hardware by comparing the parameters passed into this function
 are compared with the port names of the available MIDI drivers. Found controller scripts are
 automatically added with their input/output ports configured.

 Calling this function is optional, but can also be called multiple times in the global script scope in
 order to support alternative driver names.

**Parameters:**
- `inputs` (type String[]): the array of strings used to detect MIDI input ports, must not be `null`.
- `outputs` (type String[]): the array of strings used to detect MIDI output ports, must not be `null`.

**Returns:** type void

### defineSysexIdentityReply(java.lang.String)

Registers the `Identity Reply Universal SysEx` message (if any) that the MIDI device sends after
 receiving the `Identity Request Universal SysEx` message (`F0 7E 7F 06 01 F7`), as defined in the MIDI
 standard.

 This function may be called at the global scope of the script, but is optional. Please note that this
 function is only applicable to scripts with one MIDI input and one MIDI output. Also note that not all
 MIDI hardware supports SysEx identity messages.

**Parameters:**
- `reply` (type String): the `Identity Reply Universal SysEx` message. Must not be

**Returns:** type void

### getPreferences()

Creates a preferences object that can be used to insert settings into the Controller Preferences panel
 in Bitwig Studio.

**Returns:** type Preferences - an object that provides access to custom controller preferences

### getDocumentState()

Creates a document state object that can be used to insert settings into the Studio I/O Panel in Bitwig
 Studio.

**Returns:** type DocumentState - an object that provides access to custom document settings

### getNotificationSettings()

Returns an object that is used to configure automatic notifications. Bitwig Studio supports automatic
 visual feedback from controllers that shows up as popup notifications. For example when the selected
 track or the current device preset was changed on the controller these notifications are shown,
 depending on your configuration.

**Returns:** type NotificationSettings - a configuration object used to enable/disable the various automatic notifications supported by
         Bitwig Studio

### getProject()

Returns an object for controlling various aspects of the currently selected project.

**Returns:** type Project

### createTransport()

Returns an object for controlling and monitoring the elements of the `Transport` section in Bitwig
 Studio. This function should be called once during initialization of the script if transport access is
 desired.

**Returns:** type Transport - an object that represents the `Transport` section in Bitwig Studio.

### createGroove()

Returns an object for controlling and monitoring the `Groove` section in Bitwig Studio. This function
 should be called once during initialization of the script if groove control is desired.

**Returns:** type Groove - an object that represents the `Groove` section in Bitwig Studio.

### createApplication()

Returns an object that provides access to general application functionality, including global view
 settings, the list of open projects, and other global settings that are not related to a certain
 document.

**Returns:** type Application - an application object.

### createArranger()

Returns an object which provides access to the `Arranger` panel of Bitwig Studio. Calling this function
 is equal to `createArranger(-1)`.

**Returns:** type Arranger - an arranger object

### createArranger(int)

Returns an object which provides access to the `Arranger` panel inside the specified window.

**Parameters:**
- `window` (type int): the index of the window where the arranger panel is shown, or -1 in case the first arranger
           panel found on any window should be taken

**Returns:** type Arranger - an arranger object

### createMixer()

Returns an object which provides access to the `Mixer` panel of Bitwig Studio. Calling this function is
 equal to `createMixer(-1, null)`.

**Returns:** type Mixer - a `Mixer` object

### createMixer(java.lang.String)

Returns an object which provides access to the `Mixer` panel that belongs to the specified panel layout.
 Calling this function is equal to `createMixer(-1, panelLayout)`.

**Parameters:**
- `panelLayout` (type String): the name of the panel layout that contains the mixer panel, or `null` in case the selected
           panel layout in Bitwig Studio should be followed. Empty strings or invalid names are treated
           the same way as `null`. To receive the list of available panel layouts see

**Returns:** type Mixer - a `Mixer` object

### createMixer(int)

Returns an object which provides access to the `Mixer` panel inside the specified window. Calling this
 function is equal to `createMixer(window, null)`.

**Parameters:**
- `window` (type int): the index of the window where the mixer panel is shown, or -1 in case the first mixer panel
           found on any window should be taken

**Returns:** type Mixer - a `Mixer` object

### createMixer(java.lang.String,int)

Returns an object which provides access to the `Mixer` panel that matches the specified parameters.

**Parameters:**
- `panelLayout` (type String): the name of the panel layout that contains the mixer panel, or `null` in case the selected
           panel layout in Bitwig Studio should be followed. Empty strings or invalid names are treated
           the same way as `null`. To receive the list of available panel layouts see
- `window` (type int): the index of the window where the mixer panel is shown, or -1 in case the first mixer panel
           found on any window should be taken

**Returns:** type Mixer - a `Mixer` object

### createDetailEditor()

Returns an object which provides access to the `DetailEditor` panel of Bitwig Studio. Calling this function
 is equal to `createDetailEditor(-1)`.

**Returns:** type DetailEditor - a detail editor object

### createDetailEditor(int)

Returns an object which provides access to the `DetailEditor` panel inside the specified window.

**Parameters:**
- `window` (type int): the index of the window where the detail editor panel is shown, or -1 in case the first detail
           editor panel found on any window should be taken

**Returns:** type DetailEditor - a detail editor object

### createTrackBank(int,int,int)

Returns a track bank with the given number of tracks, sends and scenes.

 A track bank can be seen as a fixed-size window onto the list of tracks in the current document
 including their sends and scenes, that can be scrolled in order to access different parts of the track
 list. For example a track bank configured for 8 tracks can show track 1-8, 2-9, 3-10 and so on.

 The idea behind the `bank pattern` is that hardware typically is equipped with a fixed amount of channel
 strips or controls, for example consider a mixing console with 8 channels, but Bitwig Studio documents
 contain a dynamic list of tracks, most likely more tracks than the hardware can control simultaneously.
 The track bank returned by this function provides a convenient interface for controlling which tracks
 are currently shown on the hardware.

 Creating a track bank using this method will consider all tracks in the document, including effect
 tracks and the master track. Use createMainTrackBank(int, int, int) or createEffectTrackBank(int, int, int) in case
 you are only interested in tracks of a certain kind.

**Parameters:**
- `numTracks` (type int): the number of tracks spanned by the track bank
- `numSends` (type int): the number of sends spanned by the track bank
- `numScenes` (type int): the number of scenes spanned by the track bank

**Returns:** type TrackBank - an object for bank-wise navigation of tracks, sends and scenes

### createTrackBank(int,int,int,boolean)

Returns a track bank with the given number of child tracks, sends and scenes.

 A track bank can be seen as a fixed-size window onto the list of tracks in the connected track group
 including their sends and scenes, that can be scrolled in order to access different parts of the track
 list. For example a track bank configured for 8 tracks can show track 1-8, 2-9, 3-10 and so on.

 The idea behind the `bank pattern` is that hardware typically is equipped with a fixed amount of channel
 strips or controls, for example consider a mixing console with 8 channels, but Bitwig Studio documents
 contain a dynamic list of tracks, most likely more tracks than the hardware can control simultaneously.
 The track bank returned by this function provides a convenient interface for controlling which tracks
 are currently shown on the hardware.

 Creating a track bank using this method will consider all tracks in the document, including effect
 tracks and the master track. Use createMainTrackBank(int, int, int) or createEffectTrackBank(int, int, int) in case
 you are only interested in tracks of a certain kind.

**Parameters:**
- `numTracks` (type int): the number of child tracks spanned by the track bank
- `numSends` (type int): the number of sends spanned by the track bank
- `numScenes` (type int): the number of scenes spanned by the track bank
- `hasFlatTrackList` (type boolean): specifies whether the track bank should operate on a flat list of all nested child tracks or
           only on the direct child tracks of the connected group track.

**Returns:** type TrackBank - an object for bank-wise navigation of tracks, sends and scenes

### createMainTrackBank(int,int,int)

Returns a track bank with the given number of tracks, sends and scenes. Only audio tracks, instrument
 tracks and hybrid tracks are considered. For more information about track banks and the `bank pattern`
 in general, see the documentation for createTrackBank(int, int, int).

**Parameters:**
- `numTracks` (type int): the number of tracks spanned by the track bank
- `numSends` (type int): the number of sends spanned by the track bank
- `numScenes` (type int): the number of scenes spanned by the track bank

**Returns:** type TrackBank - an object for bank-wise navigation of tracks, sends and scenes

### createEffectTrackBank(int,int,int)

Returns a track bank with the given number of effect tracks, sends and scenes. Only effect tracks are
 considered. For more information about track banks and the `bank pattern` in general, see the
 documentation for createTrackBank(int, int, int).

**Parameters:**
- `numTracks` (type int): the number of tracks spanned by the track bank
- `numSends` (type int): the number of sends spanned by the track bank
- `numScenes` (type int): the number of scenes spanned by the track bank

**Returns:** type TrackBank - an object for bank-wise navigation of tracks, sends and scenes

### createEffectTrackBank(int,int)

Returns a track bank with the given number of effect tracks and scenes. Only effect tracks are
 considered. For more information about track banks and the `bank pattern` in general, see the
 documentation for createTrackBank(int, int, int).

**Parameters:**
- `numTracks` (type int): the number of tracks spanned by the track bank
- `numScenes` (type int): the number of scenes spanned by the track bank

**Returns:** type TrackBank - an object for bank-wise navigation of tracks, sends and scenes

### createMasterTrack(int)

Returns an object that represents the master track of the document.

**Parameters:**
- `numScenes` (type int): the number of scenes for bank-wise navigation of the master tracks clip launcher slots.

**Returns:** type MasterTrack - an object representing the master track.

### createArrangerCursorTrack(int,int)

Returns an object that represents the cursor item of the arranger track selection.

**Parameters:**
- `numSends` (type int): the number of sends for bank-wise navigation of the sends that are associated with the track
           selection
- `numScenes` (type int): the number of scenes for bank-wise navigation of the clip launcher slots that are associated
           with the track selection

**Returns:** type CursorTrack - an object representing the currently selected arranger track (in the future also multiple
         tracks)

### createCursorTrack(java.lang.String,int,int)

Returns an object that represents a named cursor track, that is independent from the arranger or mixer
 track selection in the user interface of Bitwig Studio.

**Parameters:**
- `name` (type String): the name of the track cursor
- `numSends` (type int): the number of sends for bank-wise navigation of the sends that are associated with the track
           selection
- `numScenes` (type int): the number of scenes for bank-wise navigation of the clip launcher slots that are associated
           with the track selection

**Returns:** type CursorTrack - an object representing the currently selected arranger track (in the future also multiple
         tracks).

### createCursorTrack(java.lang.String,java.lang.String,int,int,boolean)

Returns an object that represents a named cursor track, that is independent from the arranger or mixer
 track selection in the user interface of Bitwig Studio.

**Parameters:**
- `id` (type String)
- `name` (type String): the name of the track cursor
- `numSends` (type int): the number of sends for bank-wise navigation of the sends that are associated with the track
           selection
- `numScenes` (type int): the number of scenes for bank-wise navigation of the clip launcher slots that are associated
           with the track selection
- `shouldFollowSelection` (type boolean)

**Returns:** type CursorTrack - an object representing the currently selected arranger track (in the future also multiple
         tracks).

### createSceneBank(int)

Returns a scene bank with the given number of scenes.

 A scene bank can be seen as a fixed-size window onto the list of scenes in the current document, that
 can be scrolled in order to access different parts of the scene list. For example a scene bank
 configured for 8 scenes can show scene 1-8, 2-9, 3-10 and so on.

 The idea behind the `bank pattern` is that hardware typically is equipped with a fixed amount of channel
 strips or controls, for example consider a mixing console with 8 channels, but Bitwig Studio documents
 contain a dynamic list of scenes, most likely more scenes than the hardware can control simultaneously.
 The scene bank returned by this function provides a convenient interface for controlling which scenes
 are currently shown on the hardware.

**Parameters:**
- `numScenes` (type int): the number of scenes spanned by the track bank

**Returns:** type SceneBank - an object for bank-wise navigation of scenes

### createEditorCursorDevice()

Returns an object that represents the cursor device in devices selections made by the user in Bitwig
 Studio. Calling this method is equal to the following code: 
 var cursorTrack = createArrangerCursorTrack(numSends, numScenes);
 var cursorDevice = cursorTrack.createCursorDevice();
  To create a custom device selection that is not connected to the main device selection in the user
 interface, call cursorTrack.createCursorDevice(String name).

**Returns:** type CursorDevice - an object representing the currently selected device.

### createEditorCursorDevice(int)

Returns an object that represents the cursor device in devices selections made by the user in Bitwig
 Studio. Calling this method is equal to the following code: 
 var cursorTrack = createArrangerCursorTrack(numSends, numScenes);
 var cursorDevice = cursorTrack.createCursorDevice();
  To create a custom device selection that is not connected to the main device selection in the user
 interface, call cursorTrack.createCursorDevice(String name).

**Parameters:**
- `numSends` (type int): the number of sends that are simultaneously accessible in nested channels.

**Returns:** type CursorDevice - an object representing the currently selected device.

### createCursorClip(int,int)

**Parameters:**
- `gridWidth` (type int)
- `gridHeight` (type int)

**Returns:** type Clip

### createLauncherCursorClip(int,int)

Returns a clip object that represents the cursor of the launcher clip selection. The gridWidth and
 gridHeight parameters specify the grid dimensions used to access the note content of the clip.

**Parameters:**
- `gridWidth` (type int): the number of steps spanned by one page of the note content grid.
- `gridHeight` (type int): the number of keys spanned by one page of the note content grid.

**Returns:** type Clip - an object representing the currently selected cursor clip

### createArrangerCursorClip(int,int)

Returns a clip object that represents the cursor of the arranger clip selection. The gridWidth and
 gridHeight parameters specify the grid dimensions used to access the note content of the clip.

**Parameters:**
- `gridWidth` (type int): the number of steps spanned by one page of the note content grid.
- `gridHeight` (type int): the number of keys spanned by one page of the note content grid.

**Returns:** type Clip - an object representing the currently selected cursor clip

### createUserControls(int)

Returns an object that is used to define a bank of custom user controls. These controls are available to
 the user for free controller assignments and are typically used when bank-wise navigation is
 inconvenient.

**Parameters:**
- `numControllers` (type int): the number of controls that are available for free assignments

**Returns:** type UserControlBank - An object that represents a set of custom user controls.

### createLastClickedParameter(java.lang.String,java.lang.String)

The last clicked parameter in the gui. Can also be pinned

**Parameters:**
- `id` (type String): used for persistent state. Extensions should use different IDs for different objects, but should try to
           not change IDs in between different versions.
- `name` (type String): user facing name, used for example in context menus. Extensions may change the name in between
             different versions.

**Returns:** type LastClickedParameter

### scheduleTask(java.lang.Object,java.lang.Object[],long)

Schedules the given callback function for execution after the given delay. For timer applications call
 this method once initially and then from within the callback function.

**Parameters:**
- `callback` (type Object): the callback function that will be called
- `args` (type Object[]): that array of arguments that gets passed into the callback function, may be `null`
- `delay` (type long): the duration after which the callback function will be called in milliseconds

**Returns:** type void

### scheduleTask(java.lang.Runnable,long)

Schedules the given callback function for execution after the given delay. For timer applications call
 this method once initially and then from within the callback function.

**Parameters:**
- `callback` (type Runnable): the callback function that will be called
- `delay` (type long): the duration after which the callback function will be called in milliseconds

**Returns:** type void

### requestFlush()

Requests that the driver's flush method gets called.

**Returns:** type void

### println(java.lang.String)

Prints the given string in the control surface console window. The console window can be opened in the
 view menu of Bitwig Studio.

**Parameters:**
- `s` (type String): the string to be printed

**Returns:** type void

### errorln(java.lang.String)

Prints the given string in the control surface console window using a text style that highlights the
 string as error. The console window can be opened in the view menu of Bitwig Studio.

**Parameters:**
- `s` (type String): the error string to be printed

**Returns:** type void

### showPopupNotification(java.lang.String)

Shows a temporary text overlay on top of the application GUI, that will fade-out after a short interval.
 If the overlay is already shown, it will get updated with the given text.

**Parameters:**
- `text` (type String): the text to be shown

**Returns:** type void

### createRemoteConnection(java.lang.String,int)

Opens a TCP (Transmission Control Protocol) host socket for allowing network connections from other
 hardware and software.

**Parameters:**
- `name` (type String): a meaningful name that describes the purpose of this connection.
- `defaultPort` (type int): the port that should be used for the connection. If the port is already in use, then another
           port will be used. Check

**Returns:** type RemoteSocket - the object that represents the socket

### connectToRemoteHost(java.lang.String,int,com.bitwig.extension.callback.ConnectionEstablishedCallback)

Connects to a remote TCP (Transmission Control Protocol) socket.

**Parameters:**
- `host` (type String): the host name or IP address to connect to.
- `port` (type int): the port to connect to
- `callback` (type ConnectionEstablishedCallback): the callback function that gets called when the connection gets established. A single

**Returns:** type void

### sendDatagramPacket(java.lang.String,int,byte[])

Sends a UDP (User Datagram Protocol) packet with the given data to the specified host.

**Parameters:**
- `host` (type String): the destination host name or IP address
- `port` (type int): the destination port
- `data` (type byte[]): the data to be send. When creating a numeric byte array in JavaScript, the byte values must be
           signed (in the range -128..127).

**Returns:** type void

### addDatagramPacketObserver(java.lang.String,int,com.bitwig.extension.callback.DataReceivedCallback)

Adds an observer for incoming UDP (User Datagram Protocol) packets on the selected port.

**Parameters:**
- `name` (type String): a meaningful name that describes the purpose of this observer.
- `port` (type int): the port that should be used
- `callback` (type DataReceivedCallback): the callback function that gets called when data arrives. The function receives a single
           parameter that contains the data byte array.

**Returns:** type boolean - true if was possible to bind the port, false otherwise

### defineController(java.lang.String,java.lang.String,java.lang.String,java.lang.String)

**Parameters:**
- `vendor` (type String)
- `name` (type String)
- `version` (type String)
- `uuid` (type String)

**Returns:** type void

### createTransportSection()

**Returns:** type Transport

### createCursorTrack(int,int)

**Parameters:**
- `numSends` (type int)
- `numScenes` (type int)

**Returns:** type CursorTrack

### createGrooveSection()

**Returns:** type Groove

### createApplicationSection()

**Returns:** type Application

### createArrangerSection(int)

**Parameters:**
- `screenIndex` (type int)

**Returns:** type Arranger

### createMixerSection(java.lang.String,int)

**Parameters:**
- `perspective` (type String)
- `screenIndex` (type int)

**Returns:** type Mixer

### createTrackBankSection(int,int,int)

**Parameters:**
- `numTracks` (type int)
- `numSends` (type int)
- `numScenes` (type int)

**Returns:** type TrackBank

### createMainTrackBankSection(int,int,int)

**Parameters:**
- `numTracks` (type int)
- `numSends` (type int)
- `numScenes` (type int)

**Returns:** type TrackBank

### createEffectTrackBankSection(int,int)

**Parameters:**
- `numTracks` (type int)
- `numScenes` (type int)

**Returns:** type TrackBank

### createCursorTrackSection(int,int)

**Parameters:**
- `numSends` (type int)
- `numScenes` (type int)

**Returns:** type CursorTrack

### createMasterTrackSection(int)

**Parameters:**
- `numScenes` (type int)

**Returns:** type Track

### createCursorClipSection(int,int)

**Parameters:**
- `gridWidth` (type int)
- `gridHeight` (type int)

**Returns:** type Clip

### createCursorDeviceSection(int)

**Parameters:**
- `numControllers` (type int)

**Returns:** type CursorDevice

### createCursorDevice()

**Returns:** type CursorDevice

### createUserControlsSection(int)

**Parameters:**
- `numControllers` (type int)

**Returns:** type UserControlBank

### defineSysexDiscovery(java.lang.String,java.lang.String)

**Parameters:**
- `request` (type String)
- `reply` (type String)

**Returns:** type void

### createPopupBrowser()

Creates a PopupBrowser that represents the pop-up browser in Bitwig Studio.

**Returns:** type PopupBrowser

### defaultBeatTimeFormatter()

BeatTimeFormatter used to format beat times by default. This will be used to format beat times
 when asking for a beat time in string format without providing any formatting options. For example by
 calling DoubleValue.get().

**Returns:** type BeatTimeFormatter

### setDefaultBeatTimeFormatter(com.bitwig.extension.controller.api.BeatTimeFormatter)

Sets the BeatTimeFormatter to use by default for formatting beat times.

**Parameters:**
- `formatter` (type BeatTimeFormatter)

**Returns:** type void

### createBeatTimeFormatter(java.lang.String,int,int,int,int)

Creates a BeatTimeFormatter that can be used to format beat times.

**Parameters:**
- `separator` (type String): the character used to separate the segments of the formatted beat time, typically ":", "." or
           "-"
- `barsLen` (type int): the number of digits reserved for bars
- `beatsLen` (type int): the number of digits reserved for beats
- `subdivisionLen` (type int): the number of digits reserved for beat subdivisions
- `ticksLen` (type int): the number of digits reserved for ticks

**Returns:** type BeatTimeFormatter

### createHardwareSurface()

Creates a HardwareSurface that can contain hardware controls.

**Returns:** type HardwareSurface

### createOrHardwareActionMatcher(com.bitwig.extension.controller.api.HardwareActionMatcher,com.bitwig.extension.controller.api.HardwareActionMatcher)

Creates a HardwareActionMatcher that is matched by either of the 2 supplied action matchers.

**Parameters:**
- `matcher1` (type HardwareActionMatcher)
- `matcher2` (type HardwareActionMatcher)

**Returns:** type HardwareActionMatcher

### createOrRelativeHardwareValueMatcher(com.bitwig.extension.controller.api.RelativeHardwareValueMatcher,com.bitwig.extension.controller.api.RelativeHardwareValueMatcher)

Creates a RelativeHardwareValueMatcher that is matched by either of the 2 supplied action
 matchers.

**Parameters:**
- `matcher1` (type RelativeHardwareValueMatcher)
- `matcher2` (type RelativeHardwareValueMatcher)

**Returns:** type RelativeHardwareValueMatcher

### createOrAbsoluteHardwareValueMatcher(com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher,com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher)

Creates a AbsoluteHardwareValueMatcher that is matched by either of the 2 supplied action
 matchers.

**Parameters:**
- `matcher1` (type AbsoluteHardwareValueMatcher)
- `matcher2` (type AbsoluteHardwareValueMatcher)

**Returns:** type AbsoluteHardwareValueMatcher

### midiExpressions()

An object that can be used to generate useful MIDI expression strings which can be used in
 MidiIn.createActionMatcher(String) and other related methods.

**Returns:** type MidiExpressions

### createCallbackAction(java.lang.Runnable,java.util.function.Supplier)

Creates a HardwareActionBindable that can be bound to some HardwareAction (such as a
 button press) and when that action occurs the supplied Runnable will be run.

 This is exactly the same as createAction(Runnable, Supplier) but does not use parameter
 overloading so can be used from non type safe languages like JavaScript.

**Parameters:**
- `runnable` (type Runnable): The runnable to be run
- `descriptionProvider` (type Supplier<String>): Provider that can provide a description of what the runnable does (used for showing onscreen
           feedback or help to the user).

**Returns:** type HardwareActionBindable

### createAction(java.lang.Runnable,java.util.function.Supplier)

Creates a HardwareActionBindable that can be bound to some HardwareAction (such as a
 button press) and when that action occurs the supplied Runnable will be run.

**Parameters:**
- `runnable` (type Runnable): The runnable to be run
- `descriptionProvider` (type Supplier<String>): Provider that can provide a description of what the runnable does (used for showing onscreen
           feedback or help to the user).

**Returns:** type HardwareActionBindable

### createPressureCallbackAction(java.util.function.DoubleConsumer,java.util.function.Supplier)

Creates a HardwareActionBindable that can be bound to some HardwareAction (such as a
 button press) and when that action occurs the supplied Runnable will be run.

 This is exactly the same as createAction(DoubleConsumer, Supplier) but does not use parameter
 overloading so can be used from non type safe languages like JavaScript.

**Parameters:**
- `actionPressureConsumer` (type DoubleConsumer): Consumer that will be notified of the pressure of the action
- `descriptionProvider` (type Supplier<String>): Provider that can provide a description of what the runnable does (used for showing onscreen
           feedback or help to the user).

**Returns:** type HardwareActionBindable

### createAction(java.util.function.DoubleConsumer,java.util.function.Supplier)

Creates a HardwareActionBindable that can be bound to some HardwareAction (such as a
 button press) and when that action occurs the supplied Runnable will be run

**Parameters:**
- `actionPressureConsumer` (type DoubleConsumer): Consumer that will be notified of the pressure of the action
- `descriptionProvider` (type Supplier<String>): Provider that can provide a description of what the runnable does (used for showing onscreen
           feedback or help to the user).

**Returns:** type HardwareActionBindable

### createRelativeHardwareControlStepTarget(com.bitwig.extension.controller.api.HardwareActionBindable,com.bitwig.extension.controller.api.HardwareActionBindable)

Creates a RelativeHardwarControlBindable that can be used to step forwards or backwards when a
 RelativeHardwareControl is adjusted. A step is defined by the
 RelativeHardwareControl.setStepSize(double).

**Parameters:**
- `stepForwardsAction` (type HardwareActionBindable): The action that should happen when stepping forwards
- `stepBackwardsAction` (type HardwareActionBindable): The action that should happen when stepping backwards

**Returns:** type RelativeHardwarControlBindable

### createRelativeHardwareControlAdjustmentTarget(java.util.function.DoubleConsumer)

Creates a RelativeHardwarControlBindable that can be used to adjust some value in an arbitrary
 way.

**Parameters:**
- `adjustmentConsumer` (type DoubleConsumer): A consumer that will receive the relative adjustment amount when bound to a

**Returns:** type RelativeHardwarControlBindable

### createAbsoluteHardwareControlAdjustmentTarget(java.util.function.DoubleConsumer)

Creates a AbsoluteHardwarControlBindable that can be used to adjust some value in an arbitrary
 way.

**Parameters:**
- `adjustmentConsumer` (type DoubleConsumer): A consumer that will receive the absolute adjustment amount when bound to an

**Returns:** type AbsoluteHardwarControlBindable

### deleteObjects(java.lang.String,com.bitwig.extension.controller.api.DeleteableObject...)

It will delete multiple object within one undo step.

**Parameters:**
- `undoName` (type String)
- `objects` (type DeleteableObject...)

**Returns:** type void

### deleteObjects(com.bitwig.extension.controller.api.DeleteableObject...)

It will delete multiple object within one undo step.

**Parameters:**
- `objects` (type DeleteableObject...)

**Returns:** type void

### duplicateObjects(java.lang.String,com.bitwig.extension.controller.api.DuplicableObject...)

It will duplicate multiple object within one undo step.

**Parameters:**
- `undoName` (type String)
- `objects` (type DuplicableObject...)

**Returns:** type void

### duplicateObjects(com.bitwig.extension.controller.api.DuplicableObject...)

It will duplicate multiple object within one undo step.

**Parameters:**
- `objects` (type DuplicableObject...)

**Returns:** type void

### createInstrumentMatcher()

Creates a DeviceMatcher that will match any instrument.

**Returns:** type DeviceMatcher

### createAudioEffectMatcher()

Creates a DeviceMatcher that will match any audio effect.

**Returns:** type DeviceMatcher

### createNoteEffectMatcher()

Creates a DeviceMatcher that will match any note effect.

**Returns:** type DeviceMatcher

### createBitwigDeviceMatcher(java.util.UUID)

Creates a DeviceMatcher that will match any Bitwig native device with the supplied id.

**Parameters:**
- `id` (type UUID)

**Returns:** type DeviceMatcher

### createVST2DeviceMatcher(int)

Creates a DeviceMatcher that will match any VST2 plug-in with the supplied id.

**Parameters:**
- `id` (type int)

**Returns:** type DeviceMatcher

### createVST3DeviceMatcher(java.lang.String)

Creates a DeviceMatcher that will match any VST3 plug-in with the supplied id.

**Parameters:**
- `id` (type String)

**Returns:** type DeviceMatcher

### createActiveDeviceMatcher()

Creates a DeviceMatcher that will only match devices that are currently active.

**Returns:** type DeviceMatcher

### createFirstDeviceInChainMatcher()

Creates a DeviceMatcher that will only match devices if it is the last device in the chain.

**Returns:** type DeviceMatcher

### createLastDeviceInChainMatcher()

Creates a DeviceMatcher that will only match devices if it is the last device in the chain.

**Returns:** type DeviceMatcher

### createOrDeviceMatcher(com.bitwig.extension.controller.api.DeviceMatcher...)

Creates a DeviceMatcher that matches a device if any of the supplied matchers match the device.

**Parameters:**
- `deviceMatchers` (type DeviceMatcher...)

**Returns:** type DeviceMatcher

### createAndDeviceMatcher(com.bitwig.extension.controller.api.DeviceMatcher...)

Creates a DeviceMatcher that matches a device if all the supplied matchers match the device.

**Parameters:**
- `deviceMatchers` (type DeviceMatcher...)

**Returns:** type DeviceMatcher

### createNotDeviceMatcher(com.bitwig.extension.controller.api.DeviceMatcher)

Creates a DeviceMatcher that matches a device if the supplied matcher does not match the device.

**Parameters:**
- `deviceMatcher` (type DeviceMatcher)

**Returns:** type DeviceMatcher

### createMasterRecorder()

Creates a MasterRecorder.

**Returns:** type MasterRecorder

### createAudioIoDeviceHardwareAddressMatcher(java.lang.String)

Creates a matcher that matches devices with the given hardware address.

**Parameters:**
- `hardwareAddress` (type String)

**Returns:** type AudioIoDeviceMatcher

### createUsbAudioIoDeviceMatcher(int,int)

Creates a matcher that matches devices with the given USB vendor and product id.

**Parameters:**
- `vendorId` (type int)
- `productId` (type int)

**Returns:** type AudioIoDeviceMatcher

### createAudioHardwareOutputInfo(com.bitwig.extension.controller.api.AudioIoDeviceMatcher,int[])

Creates a AudioHardwareIoInfo for the specified output.

**Parameters:**
- `matcher` (type AudioIoDeviceMatcher)
- `channels` (type int[]): zero based channel indices

**Returns:** type AudioHardwareIoInfo

### createAudioHardwareInputInfo(com.bitwig.extension.controller.api.AudioIoDeviceMatcher,int[])

Creates a AudioHardwareIoInfo for the specified input.

**Parameters:**
- `matcher` (type AudioIoDeviceMatcher)
- `channels` (type int[]): zero based channel indices

**Returns:** type AudioHardwareIoInfo

### getHostApiVersion()

Returns the latest supported API version of the host application.

**Returns:** type int - the latest supported API version of the host application

### getHostVendor()

Returns the vendor of the host application.

**Returns:** type String - the vendor of the host application

### getHostProduct()

Returns the product name of the host application.

**Returns:** type String - the product name of the host application

### getHostVersion()

Returns the version number of the host application.

**Returns:** type String - the version number of the host application

### getPlatformType()

The platform type that this host is running on.

**Returns:** type PlatformType

### setErrorReportingEMail(java.lang.String)

Sets an email address to use for reporting errors found in this script.

**Parameters:**
- `address` (type String)

**Returns:** type void

### getOscModule()

Gets the OpenSoundControl module.

**Returns:** type OscModule

### allocateMemoryBlock(int)

Allocates some memory that will be automatically freed once the extension exits.

**Parameters:**
- `size` (type int)

**Returns:** type MemoryBlock

### createBitmap(int,int,com.bitwig.extension.api.graphics.BitmapFormat)

Creates an offscreen bitmap that the extension can use to render into. The memory used by this bitmap is
 guaranteed to be freed once this extension exits.

**Parameters:**
- `width` (type int)
- `height` (type int)
- `format` (type BitmapFormat)

**Returns:** type Bitmap

### loadFontFace(java.lang.String)

Loads a font.
 The memory used by this font is guaranteed to be freed once this extension exits.

**Parameters:**
- `path` (type String)

**Returns:** type FontFace

### createFontOptions()

Creates a new FontOptions.
 This object is used to configure how the GraphicOutput will display text.
 The memory used by this object is guaranteed to be freed once this extension exits.

**Returns:** type FontOptions

### loadPNG(java.lang.String)

Loads a PNG image.
 The memory used by this image is guaranteed to be freed once this extension exits.

**Parameters:**
- `path` (type String)

**Returns:** type Image

### loadSVG(java.lang.String,double)

Loads a SVG image.
 The memory used by this image is guaranteed to be freed once this extension exits.

**Parameters:**
- `path` (type String)
- `scale` (type double)

**Returns:** type Image

