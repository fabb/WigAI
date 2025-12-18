# HardwareControl

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Some kind of physical control on a piece of hardware (such as a knob, button, slider etc).

**Extends:** `com.bitwig.extension.controller.api.HardwareElement`

## Methods

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

