# SettableBooleanValue

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent boolean values.

**Extends:** `com.bitwig.extension.controller.api.BooleanValue`, `com.bitwig.extension.controller.api.HardwareActionBindable`

## Methods

### set(boolean)

Sets the internal value.

**Parameters:**
- `value` (type boolean): the new boolean value.

**Returns:** type void

### toggle()

Toggles the current state. In case the current value is `false`, the new value will be `true` and the
 other way round.

**Returns:** type void

### toggleAction()

**Returns:** type HardwareActionBindable

### setToTrueAction()

**Returns:** type HardwareActionBindable

### setToFalseAction()

**Returns:** type HardwareActionBindable

### get()

Gets the current value.

**Returns:** type boolean

### getAsBoolean()

**Returns:** type boolean

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

### addBinding(com.bitwig.extension.controller.api.HardwareAction)

Binds this target to the supplied HardwareAction so that when the hardware action occurs this
 target is invoked.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `action` (type HardwareAction)

**Returns:** type HardwareActionBinding

### invoke()

Invokes the action.

**Returns:** type void

