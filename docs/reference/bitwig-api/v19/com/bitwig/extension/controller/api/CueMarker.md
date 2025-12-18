# CueMarker

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

This interface defines access to the common attributes and operations of cue markers.

**Extends:** `com.bitwig.extension.controller.api.ObjectProxy`, `com.bitwig.extension.controller.api.DeleteableObject`, `com.bitwig.extension.controller.api.DuplicableObject`

## Methods

### launch(boolean)

Launches playback at the marker position.

**Parameters:**
- `quantized` (type boolean): Specified if the cue marker should be launched quantized or immediately

**Returns:** type void

### name()

Gets a representation of the marker name.

**Returns:** type SettableStringValue

### getColor()

Gets a representation of the marker color.

**Returns:** type ColorValue

### position()

Gets a representation of the markers beat-time position in quarter-notes.

**Returns:** type SettableBeatTimeValue

### getName()

Gets a representation of the marker name.

**Returns:** type StringValue

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

