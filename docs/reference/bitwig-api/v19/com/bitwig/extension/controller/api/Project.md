# Project

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

An interface for representing the current project.

**Extends:** `com.bitwig.extension.controller.api.ObjectProxy`

## Methods

### getRootTrackGroup()

Returns an object that represents the root track group of the active Bitwig Studio project.

**Returns:** type Track - the root track group of the currently active project

### getShownTopLevelTrackGroup()

Returns an object that represents the top level track group as shown in the arranger/mixer of the active
 Bitwig Studio project.

**Returns:** type Track - the shown top level track group of the currently active project

### createScene()

Creates a new empty scene as the last scene in the project.

**Returns:** type void

### createSceneFromPlayingLauncherClips()

Creates a new scene (using an existing empty scene if possible) from the clips that are currently
 playing in the clip launcher.

**Returns:** type void

### cueVolume()

The volume used for cue output.

**Returns:** type Parameter

### cueMix()

Mix between cue bus and the studio bus (master).

**Returns:** type Parameter

### unsoloAll()

Sets the solo state of all tracks to off.

**Returns:** type void

### hasSoloedTracks()

**Returns:** type BooleanValue

### unmuteAll()

Sets the mute state of all tracks to off.

**Returns:** type void

### hasMutedTracks()

Value that indicates if the project has muted tracks or not.

**Returns:** type BooleanValue

### unarmAll()

Sets the arm state of all tracks to off.

**Returns:** type void

### hasArmedTracks()

Value that indicates if the project has armed tracks or not.

**Returns:** type BooleanValue

### isModified()

Value that indicates if the project is modified or not.

**Returns:** type BooleanValue

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

