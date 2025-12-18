# NoteLatch

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Creates a proxy object to the NoteInput's NoteLatch component.

**Extends:** `com.bitwig.extension.controller.api.ObjectProxy`

## Methods

### isEnabled()

Returns an object to enable or disable the note latch component.

**Returns:** type SettableBooleanValue

### mode()

Returns an object to configure the note latch mode.
 Possible values:
  - chord
  - toggle
  - velocity

**Returns:** type SettableEnumValue

### mono()

Only one note at a time.

**Returns:** type SettableBooleanValue

### velocityThreshold()

The velocity threshold used by the velocity latch mode.

**Returns:** type SettableIntegerValue

### activeNotes()

How many notes are being latched.

**Returns:** type IntegerValue

### releaseNotes()

Release all notes being latched.

**Returns:** type void

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

