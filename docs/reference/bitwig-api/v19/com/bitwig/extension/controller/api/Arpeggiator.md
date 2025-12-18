# Arpeggiator

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Proxy to an arpeggiator component.

**Extends:** `com.bitwig.extension.controller.api.ObjectProxy`

## Methods

### mode()

Returns an object to configure the arpeggiator mode.
 Possible values:
  - all
  - up
  - up-down
  - up-then-down
  - down
  - down-up
  - down-then-up
  - flow
  - random
  - converge-up
  - converge-down
  - diverge-up
  - diverge-down
  - thumb-up
  - thumb-down
  - pinky-up
  - pinky-down

**Returns:** type SettableEnumValue

### octaves()

Returns an object to configure the range in octaves.
 The range is between 0 and 8.

**Returns:** type SettableIntegerValue

### isEnabled()

Returns an object to enable or disable the note repeat component.

**Returns:** type SettableBooleanValue

### isFreeRunning()

If true the arpeggiator will not try to sync to the transport.

**Returns:** type SettableBooleanValue

### shuffle()

Return an object to configure the note repeat to use shuffle or not.

**Returns:** type SettableBooleanValue

### rate()

Returns an object to configure the note repeat rate in beats.

**Returns:** type SettableDoubleValue

### gateLength()

Returns an object to configure the note length, expressed as a ratio of the period.
 Must be between 1/32 and 8.

**Returns:** type SettableDoubleValue

### enableOverlappingNotes()

Let the arpeggiator play overlapping notes.

**Returns:** type SettableBooleanValue

### usePressureToVelocity()

Will use the note pressure to determine the velocity of arpeggiated notes.

**Returns:** type SettableBooleanValue

### releaseNotes()

Release all notes being played.

**Returns:** type void

### humanize()

Will introduce human-like errors.
 Between 0 and 1.

**Returns:** type SettableDoubleValue

### terminateNotesImmediately()

If set to true, it will terminate the playing note as soon as it is released, otherwise it will
 be held until its computed note-off time.

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

