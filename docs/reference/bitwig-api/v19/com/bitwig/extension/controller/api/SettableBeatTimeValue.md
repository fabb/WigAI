# SettableBeatTimeValue

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

**Extends:** `com.bitwig.extension.controller.api.BeatTimeValue`, `com.bitwig.extension.controller.api.SettableDoubleValue`, `com.bitwig.extension.controller.api.RelativeHardwarControlBindable`

## Methods

### setRaw(double)

The same as the set method.

**Parameters:**
- `value` (type double)

**Returns:** type void

### incRaw(double)

The same as the inc method.

**Parameters:**
- `amount` (type double)

**Returns:** type void

### beatStepper()

Stepper that steps through beat values. This can be used as a target for a
 RelativeHardwareControl.

**Returns:** type RelativeHardwarControlBindable

### addRawValueObserver(com.bitwig.extension.callback.DoubleValueChangedCallback)

Add an observer which receives the internal raw of the parameter as floating point.

**Parameters:**
- `callback` (type DoubleValueChangedCallback): a callback function that receives a single numeric parameter with double precision.

**Returns:** type void

### getFormatted(com.bitwig.extension.controller.api.BeatTimeFormatter)

Gets the current beat time formatted according to the supplied formatter.

**Parameters:**
- `formatter` (type BeatTimeFormatter)

**Returns:** type String

### getFormatted()

Gets the current beat time formatted according to the default beat time formatter.

**Returns:** type String

### addTimeObserver(java.lang.String,int,int,int,int,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the internal beat time value as formatted text, for example
 "012:03:00:01".

**Parameters:**
- `separator` (type String): the character used to separate the segments of the formatted beat time, typically ":", "." or
           "-"
- `barsLen` (type int): the number of digits reserved for bars
- `beatsLen` (type int): the number of digits reserved for beats
- `subdivisionLen` (type int): the number of digits reserved for beat subdivisions
- `ticksLen` (type int): the number of digits reserved for ticks
- `callback` (type StringValueChangedCallback): a callback function that receives a single string parameter

**Returns:** type void

### get()

Gets the current value.

**Returns:** type double

### getAsDouble()

**Returns:** type double

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

### set(double)

Sets the internal value.

**Parameters:**
- `value` (type double): the new integer value.

**Returns:** type void

### inc(double)

Increases/decrease the internal value by the given amount.

**Parameters:**
- `amount` (type double): the integer amount to increase

**Returns:** type void

### addBinding(com.bitwig.extension.controller.api.RelativeHardwareControl)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)

**Returns:** type RelativeHardwareControlBinding

### addBindingWithSensitivity(com.bitwig.extension.controller.api.RelativeHardwareControl,double)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)
- `sensitivity` (type double)

**Returns:** type RelativeHardwareControlBinding

