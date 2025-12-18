# InsertionPoint

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Defines an insertion point where various objects can be inserted as if the user had dragged and dropped
 them to this insertion point (e.g with the mouse). Some things may not make sense to insert in which case
 nothing happens.

## Methods

### copyTracks(com.bitwig.extension.controller.api.Track...)

Copies the supplied tracks to this insertion point. If it's not possible to do so then this does
 nothing.

**Parameters:**
- `tracks` (type Track...)

**Returns:** type void

### moveTracks(com.bitwig.extension.controller.api.Track...)

Moves the supplied tracks to this insertion point. If it's not possible to do so then this does nothing.

**Parameters:**
- `tracks` (type Track...)

**Returns:** type void

### copyDevices(com.bitwig.extension.controller.api.Device...)

Copies the supplied devices to this insertion point. If it's not possible to do so then this does
 nothing.

**Parameters:**
- `devices` (type Device...)

**Returns:** type void

### moveDevices(com.bitwig.extension.controller.api.Device...)

Moves the supplied devices to this insertion point. If it's not possible to do so then this does
 nothing.

**Parameters:**
- `devices` (type Device...)

**Returns:** type void

### copySlotsOrScenes(com.bitwig.extension.controller.api.ClipLauncherSlotOrScene...)

Copies the supplied slots or scenes to this insertion point. If it's not possible to do so then this
 does nothing.

**Parameters:**
- `clipLauncherSlotOrScenes` (type ClipLauncherSlotOrScene...)

**Returns:** type void

### moveSlotsOrScenes(com.bitwig.extension.controller.api.ClipLauncherSlotOrScene...)

Moves the supplied slots or scenes to this insertion point. If it's not possible to do so then this does
 nothing.

**Parameters:**
- `clipLauncherSlotOrScenes` (type ClipLauncherSlotOrScene...)

**Returns:** type void

### insertFile(java.lang.String)

Inserts the supplied file at this insertion point. If it's not possible to do so then this does nothing.

**Parameters:**
- `path` (type String)

**Returns:** type void

### insertBitwigDevice(java.util.UUID)

Inserts a Bitwig device with the supplied id at this insertion point. If the device is unknown or it's
 not possible to insert a device here then his does nothing.

**Parameters:**
- `id` (type UUID): The Bitwig device id to insert

**Returns:** type void

### insertVST2Device(int)

Inserts a VST2 plugin device with the supplied id at this insertion point. If the plug-in is unknown, or
 it's not possible to insert a plug-in here then his does nothing.

**Parameters:**
- `id` (type int): The VST2 plugin id to insert

**Returns:** type void

### insertVST3Device(java.lang.String)

Inserts a VST3 plugin device with the supplied id at this insertion point. If the plug-in is unknown, or
 it's not possible to insert a plug-in here then his does nothing.

**Parameters:**
- `id` (type String): The VST3 plugin id to insert

**Returns:** type void

### insertCLAPDevice(java.lang.String)

Inserts a CLAP plugin device with the supplied id at this insertion point. If the plug-in is unknown, or
 it's not possible to insert a plug-in here then his does nothing.

**Parameters:**
- `id` (type String): The CLAP plugin id to insert

**Returns:** type void

### paste()

Pastes the contents of the clipboard at this insertion point.

**Returns:** type void

### browse()

Starts browsing using the popup browser for something to insert at this insertion point.

**Returns:** type void

### browseAction()

**Returns:** type HardwareActionBindable

