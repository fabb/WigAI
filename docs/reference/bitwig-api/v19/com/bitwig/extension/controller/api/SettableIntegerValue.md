# SettableIntegerValue

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent integer values.

**Extends:** `com.bitwig.extension.controller.api.IntegerValue`, `com.bitwig.extension.controller.api.RelativeHardwarControlBindable`

## Methods

### set(int)

Sets the internal value.

**Parameters:**
- `value` (type int): the new integer value.

**Returns:** type void

### inc(int)

Increases/decrease the internal value by the given amount.

**Parameters:**
- `amount` (type int): the integer amount to increase

**Returns:** type void

### get()

Gets the current value.

**Returns:** type int

### getAsInt()

**Returns:** type int

### addValueObserver(com.bitwig.extension.callback.IntegerValueChangedCallback,int)

Adds an observer that is notified when this value changes. This is intended to aid in backwards
 compatibility for drivers written to the version 1 API.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): The callback to notify with the new value
- `valueWhenUnassigned` (type int): The value that the callback will be notified with if this value is not currently assigned to
           anything.

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

