# AbsoluteHardwareControl

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents a hardware control that can input and absolute value (for example, a slider, knob or foot
 pedal).

**Extends:** `com.bitwig.extension.controller.api.ContinuousHardwareControl`

## Methods

### setAdjustValueMatcher(com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher)

Sets the AbsoluteHardwareValueMatcher that can be used to detect when the user adjusts the
 hardware control's value.

**Parameters:**
- `matcher` (type AbsoluteHardwareValueMatcher)

**Returns:** type void

### value()

The current value of this hardware control (0..1)

**Returns:** type DoubleValue

### disableTakeOver()

Determines if this hardware control should immediately take over the parameter it is bound to rather
 than respecting the user's current take over mode.

 This is useful for motorized sliders for example, where the slider is already at the value of the bound
 parameter.

**Returns:** type void

### addBindingWithRange(com.bitwig.extension.controller.api.AbsoluteHardwarControlBindable,double,double)

Adds a new binding from this hardware control to the supplied target.

**Parameters:**
- `target` (type AbsoluteHardwarControlBindable)
- `minNormalizedValue` (type double)
- `maxNormalizedValue` (type double)

**Returns:** type AbsoluteHardwareControlBinding

### setBindingWithRange(com.bitwig.extension.controller.api.AbsoluteHardwarControlBindable,double,double)

Convenience methods that ensures there is only a single binding to the supplied target. This is
 equivalent to calling HardwareBindingSource.clearBindings() and then
 addBindingWithRange(AbsoluteHardwarControlBindable, double, double)

**Parameters:**
- `target` (type AbsoluteHardwarControlBindable)
- `minNormalizedValue` (type double)
- `maxNormalizedValue` (type double)

**Returns:** type AbsoluteHardwareControlBinding

### hardwareButton()

An optional button that can be associated with this control when this control can also act as a button
 (e.g by pressing down on it).

**Returns:** type HardwareButton

### setHardwareButton(com.bitwig.extension.controller.api.HardwareButton)

Sets an optional button that can be associated with this control when this control can also act as a
 button (e.g by pressing down on it).

**Parameters:**
- `button` (type HardwareButton)

**Returns:** type void

### targetName()

The name of the target that this hardware control has been bound to.

**Returns:** type StringValue

### targetValue()

The value of the target that this hardware control has been bound to (0..1).

**Returns:** type DoubleValue

### targetDisplayedValue()

Value that represents a formatted text representation of the target value whenever the value changes.

**Returns:** type StringValue

### modulatedTargetValue()

The value of the target that this hardware control has been bound to (0..1).

**Returns:** type DoubleValue

### modulatedTargetDisplayedValue()

Value that represents a formatted text representation of the target's modulated value whenever the value
 changes.

**Returns:** type StringValue

### isUpdatingTargetValue()

Can be called from the targetValue() changed callback to check if this control is responsible
 for changing the target value or not.

**Returns:** type BooleanValue

### hasTargetValue()

Value that indicates if this hardware control has a target value that it changes or not.

**Returns:** type BooleanValue

### getName()

The name of this hardware control. This will be shown in the mapping browser, for example. It should
 provide enough information for the user to understand which control is being referred to. If the name is
 not provided then the label will be used, and if that is not provided then the id will be used.

**Returns:** type String

### setName(java.lang.String)

The name of this hardware control. This will be shown in the mapping browser, for example. It should
 provide enough information for the user to understand which control is being referred to. If the name is
 not provided then the label will be used, and if that is not provided then the id will be used.

**Parameters:**
- `name` (type String)

**Returns:** type void

### setIndexInGroup(int)

If this control is part of group of related controls then this specifies the index in that group.
 This index is used to automatically indicate a mapping color on a parameter that this hardware
 control gets bound to.

**Parameters:**
- `index` (type int)

**Returns:** type void

### beginTouchAction()

Action that happens when the user touches this control.

**Returns:** type HardwareAction

### endTouchAction()

Action that happens when the user stops touching this control.

**Returns:** type HardwareAction

### isBeingTouched()

Value that indicates if this control is being touched or not.

**Returns:** type BooleanValue

### backgroundLight()

Optional light that is in the background of this control.

**Returns:** type HardwareLight

### setBackgroundLight(com.bitwig.extension.controller.api.HardwareLight)

Sets the optional light that is in the background of this control.

**Parameters:**
- `light` (type HardwareLight)

**Returns:** type void

### getId()

The unique id associated with this element.

**Returns:** type String

### getLabel()

An optional label associated with this element.

**Returns:** type String

### setLabel(java.lang.String)

Sets the label for this hardware control as written on the hardware.

**Parameters:**
- `label` (type String)

**Returns:** type void

### getLabelColor()

The color of the label.

**Returns:** type Color

### setLabelColor(com.bitwig.extension.api.Color)

Sets the color of the label.

**Parameters:**
- `color` (type Color)

**Returns:** type void

### getLabelPosition()

RelativePosition that defines where the label is.

**Returns:** type RelativePosition

### setLabelPosition(com.bitwig.extension.controller.api.RelativePosition)

**Parameters:**
- `position` (type RelativePosition)

**Returns:** type void

### setBounds(double,double,double,double)

The physical bounds of this hardware element on the controller.

**Parameters:**
- `xInMM` (type double)
- `yInMM` (type double)
- `widthInMM` (type double)
- `heightInMM` (type double)

**Returns:** type void

### getX()

**Returns:** type double

### getY()

**Returns:** type double

### getWidth()

**Returns:** type double

### getHeight()

**Returns:** type double

### canBindTo(java.lang.Object)

Checks if it is possible to make a binding from this source to the supplied target object.

**Parameters:**
- `target` (type Object)

**Returns:** type boolean

### addBinding(com.bitwig.extension.controller.api.HardwareBindable)

Binds this source to the supplied target and returns the created binding. This can only be called if the
 canBindTo(Object) returns true.

**Parameters:**
- `target` (type HardwareBindable)

**Returns:** type HardwareBindingType

### clearBindings()

Clears all bindings from this source to its targets.

**Returns:** type void

### setBinding(com.bitwig.extension.controller.api.HardwareBindable)

Ensures there is a single binding to the supplied target.

 This is a convenience method that is equivalent to calling clearBindings() and the
 addBinding(HardwareBindable)

**Parameters:**
- `target` (type HardwareBindable)

**Returns:** type HardwareBindingType

