# SourceSelector

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instance of this class represent sources selectors in Bitwig Studio, which are shown as choosers in the
 user interface and contain entries for either note inputs or audio inputs or both.

 The most prominent source selector in Bitwig Studio is the one shown in the track IO section, which can be
 accessed via the API by calling Track.getSourceSelector().

**Extends:** `com.bitwig.extension.controller.api.ObjectProxy`

## Methods

### getHasNoteInputSelected()

Returns an object that indicates if the source selector has note inputs enabled.

**Returns:** type BooleanValue - a boolean value object

### hasNoteInputSelected()

Returns an object that indicates if the source selector has note inputs enabled.

**Returns:** type BooleanValue - a boolean value object

### getHasAudioInputSelected()

Returns an object that indicates if the source selector has audio inputs enabled.

**Returns:** type BooleanValue - a boolean value object

### hasAudioInputSelected()

Returns an object that indicates if the source selector has audio inputs enabled.

**Returns:** type BooleanValue - a boolean value object

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

