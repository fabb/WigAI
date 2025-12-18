# RemoteControl

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents a remote control in Bitwig Studio.

**Extends:** `com.bitwig.extension.controller.api.Parameter`, `com.bitwig.extension.controller.api.DeleteableObject`

## Methods

### name()

The name of the parameter.

**Returns:** type SettableStringValue

### isBeingMapped()

Returns an object indicating whether this remote control's mapping is being changed. An unmapped remote control
 slot can be mapped by setting this to true.

**Returns:** type SettableBooleanValue

### value()

Gets the current value of this parameter.

**Returns:** type SettableRangedValue

### modulatedValue()

Gets the modulated value of this parameter.

**Returns:** type RangedValue

### addNameObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Adds an observer which reports changes to the name of the automated parameter. The callback will get
 called at least once immediately after calling this method for reporting the current name.

**Parameters:**
- `maxChars` (type int): maximum length of the string sent to the observer
- `textWhenUnassigned` (type String): the default text to use
- `callback` (type StringValueChangedCallback): a callback function that receives a single string parameter

**Returns:** type void

### addValueDisplayObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Adds an observer which sends a formatted text representation of the value whenever the value changes.
 The callback will get called at least once immediately after calling this method for reporting the
 current state.

**Parameters:**
- `maxChars` (type int): maximum length of the string sent to the observer
- `textWhenUnassigned` (type String): the default text to use
- `callback` (type StringValueChangedCallback): a callback function that receives a single string parameter

**Returns:** type void

### reset()

Resets the value to its default.

**Returns:** type void

### touch(boolean)

Touch (or un-touch) the value for automation recording.

**Parameters:**
- `isBeingTouched` (type boolean): `true` for touching, `false` for un-touching

**Returns:** type void

### setIndication(boolean)

Specifies if this value should be indicated as mapped in Bitwig Studio, which is visually shown as
 colored dots or tinting on the parameter controls.

**Parameters:**
- `shouldIndicate` (type boolean): `true` in case visual indications should be shown in Bitwig Studio, `false` otherwise

**Returns:** type void

### setLabel(java.lang.String)

Specifies a label for the mapped hardware parameter as shown in Bitwig Studio, for example in menu items
 for learning controls.

**Parameters:**
- `label` (type String): the label to be shown in Bitwig Studio

**Returns:** type void

### restoreAutomationControl()

Restores control of this parameter to automation playback.

**Returns:** type void

### hasAutomation()

Boolean value that is true if the parameter has automation data.

**Returns:** type BooleanValue

### deleteAllAutomation()

Deletes all automation for this parameter.

**Returns:** type void

### set(double)

Sets the value in an absolute fashion as a value between 0 .. 1 where 0 represents the minimum value and
 1 the maximum. The value may not be set immediately if the user has configured a take over strategy for
 the controller.

**Parameters:**
- `value` (type double): absolute value [0 .. 1]

**Returns:** type void

### setImmediately(double)

Sets the value in an absolute fashion as a value between 0 .. 1 where 0 represents the minimum value and
 1 the maximum. The value change is applied immediately and does not care about what take over mode the
 user has selected. This is useful if the value does not need take over (e.g. a motorized slider).

**Parameters:**
- `value` (type double): absolute value [0 .. 1]

**Returns:** type void

### set(java.lang.Number,java.lang.Number)

Sets the value in an absolute fashion. The value will be scaled according to the given resolution.

 Typically the resolution would be specified as the amount of steps the hardware control provides (for
 example 128) and just pass the integer value as it comes from the MIDI device. The host application will
 take care of scaling it.

**Parameters:**
- `value` (type Number): integer number in the range [0 .. resolution-1]
- `resolution` (type Number): the resolution used for scaling @ if passed-in parameters are null

**Returns:** type void

### inc(double)

Increments or decrements the value by a normalized amount assuming the whole range of the value is 0 ..
 1. For example to increment by 10% you would use 0.1 as the increment.

**Parameters:**
- `increment` (type double)

**Returns:** type void

### inc(java.lang.Number,java.lang.Number)

Increments or decrements the value according to the given increment and resolution parameters.

 Typically the resolution would be specified as the amount of steps the hardware control provides (for
 example 128) and just pass the integer value as it comes from the MIDI device. The host application will
 take care of scaling it.

**Parameters:**
- `increment` (type Number): the amount that the current value is increased by
- `resolution` (type Number): the resolution used for scaling

**Returns:** type void

### setRaw(double)

Set the internal (raw) value.

**Parameters:**
- `value` (type double): the new value with double precision. Range is undefined.

**Returns:** type void

### incRaw(double)

Increments / decrements the internal (raw) value.

**Parameters:**
- `delta` (type double): the amount that the current internal value get increased by.

**Returns:** type void

### addBinding(com.bitwig.extension.controller.api.AbsoluteHardwareControl)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type AbsoluteHardwareControl)

**Returns:** type AbsoluteHardwareControlBinding - The newly created binding

### addBindingWithRange(com.bitwig.extension.controller.api.AbsoluteHardwareControl,double,double)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way. This target will be adjusted within the supplied normalized
 range.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type AbsoluteHardwareControl)
- `minNormalizedValue` (type double)
- `maxNormalizedValue` (type double)

**Returns:** type AbsoluteHardwareControlBinding - The newly created binding

### addBinding(com.bitwig.extension.controller.api.RelativeHardwareControl)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)

**Returns:** type RelativeHardwareControlToRangedValueBinding

### addBindingWithRange(com.bitwig.extension.controller.api.RelativeHardwareControl,double,double)

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)
- `minNormalizedValue` (type double)
- `maxNormalizedValue` (type double)

**Returns:** type RelativeHardwareControlBinding

### addBindingWithRangeAndSensitivity(com.bitwig.extension.controller.api.RelativeHardwareControl,double,double,double)

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)
- `minNormalizedValue` (type double)
- `maxNormalizedValue` (type double)
- `sensitivity` (type double)

**Returns:** type RelativeHardwareControlToRangedValueBinding

### addBindingWithSensitivity(com.bitwig.extension.controller.api.RelativeHardwareControl,double)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)
- `sensitivity` (type double)

**Returns:** type RelativeHardwareControlToRangedValueBinding

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

### exists()

Returns a value object that indicates if the object being proxied exists, or if it has content.

**Returns:** type BooleanValue

### createEqualsValue(com.bitwig.extension.controller.api.ObjectProxy)

Creates a BooleanValue that determines this proxy is considered equal to another proxy. For this
 to be the case both proxies need to be proxying the same target object.

**Parameters:**
- `other` (type ObjectProxy)

**Returns:** type BooleanValue

### deleteObject()

Deletes this object from the document.

 If you want to delete multiple objects at once, see Host.deleteObjects().

**Returns:** type void

### deleteObjectAction()

Deletes this object from the document.

**Returns:** type HardwareActionBindable

