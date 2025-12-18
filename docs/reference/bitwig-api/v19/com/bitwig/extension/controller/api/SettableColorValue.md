# SettableColorValue

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

**Extends:** `com.bitwig.extension.controller.api.ColorValue`

## Methods

### set(float,float,float)

Sets the internal value.

**Parameters:**
- `red` (type float)
- `green` (type float)
- `blue` (type float)

**Returns:** type void

### set(float,float,float,float)

Sets the internal value.

**Parameters:**
- `red` (type float)
- `green` (type float)
- `blue` (type float)
- `alpha` (type float)

**Returns:** type void

### set(com.bitwig.extension.api.Color)

Sets the color.

**Parameters:**
- `color` (type Color)

**Returns:** type void

### red()

Gets the red component of the current value.

**Returns:** type float

### green()

Gets the green component of the current value.

**Returns:** type float

### blue()

Gets the blue component of the current value.

**Returns:** type float

### alpha()

Gets the alpha component of the current value.

**Returns:** type float

### get()

**Returns:** type Color

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

