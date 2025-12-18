# RangedValue

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent numeric values that have an upper and lower limit.

**Extends:** `com.bitwig.extension.controller.api.Value`, `java.util.function.DoubleSupplier`

## Methods

### get()

The current value normalized between 0..1 where 0 represents the minimum value and 1 the maximum.

**Returns:** type double

### getRaw()

Gets the current value.

**Returns:** type double

### getAsDouble()

**Returns:** type double

### getOrigin()

The normalized origin of this value.

 For example, the origin for a pan value is 0.5 (representing center), but the origin for a level value is 0.

**Returns:** type DoubleValue

### discreteValueCount()

The number of discrete steps available in the range, or -1 for continuous value ranges.

**Returns:** type IntegerValue

### discreteValueNames()

Gets the name for @param index with the index between 0 and discreteValueCount() - 1.
 WARNING: the returned value may have fewer entries than the discreteValueCount.

**Returns:** type StringArrayValue

### displayedValue()

Value that represents a formatted text representation of the value whenever the value changes.

**Returns:** type StringValue

### addValueObserver(int,com.bitwig.extension.callback.IntegerValueChangedCallback)

Adds an observer which receives the normalized value of the parameter as an integer number within the
 range [0..range-1].

**Parameters:**
- `range` (type int): the range used to scale the value when reported to the callback
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single double parameter

**Returns:** type void

### addRawValueObserver(com.bitwig.extension.callback.DoubleValueChangedCallback)

Add an observer which receives the internal raw of the parameter as floating point.

**Parameters:**
- `callback` (type DoubleValueChangedCallback): a callback function that receives a single numeric parameter with double precision.

**Returns:** type void

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

