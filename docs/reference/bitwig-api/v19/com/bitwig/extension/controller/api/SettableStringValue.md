# SettableStringValue

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface implement the Value interface for string values.

**Extends:** `com.bitwig.extension.controller.api.StringValue`

## Methods

### set(java.lang.String)

Sets the value object to the given string.

**Parameters:**
- `value` (type String): the new value string

**Returns:** type void

### get()

Gets the current value.

**Returns:** type String

### getLimited(int)

Gets the current value and tries to intelligently limit it to the supplied length in the best way
 possible.

**Parameters:**
- `maxLength` (type int)

**Returns:** type String

### markInterested()

Marks this value as being of interest to the driver. This can only be called once during the driver's
 init method. A value that is of interest to the driver can be obtained using the value's get method. If
 a value has not been marked as interested then an error will be reported if the driver attempts to get
 the current value. Adding an observer to a value will automatically mark this value as interested.

**Returns:** type void

### addValueObserver(ObserverType)

Registers an observer that reports the current value.

**Parameters:**
- `callback` (type ObserverType): a callback function that receives a single parameter

**Returns:** type void

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

