# MultiStateHardwareLight

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents a physical hardware light on a controller. The light has an on/off state and may also be
 optionally colored.

**Extends:** `com.bitwig.extension.controller.api.HardwareLight`

## Methods

### state()

Object that represents the current state of this light. The interpretation of this value is entirely up
 to the implementation.

**Returns:** type ObjectHardwareProperty<InternalHardwareLightState>

### setColorToStateFunction(java.util.function.Function)

Sets a function that can be used to convert a color to the closest possible state representing that
 color. Once this function has been provided it is possible to then use the convenient
 setColor(Color) and setColorSupplier(Supplier) methods.

**Parameters:**
- `function` (type Function<Color,InternalHardwareLightState>)

**Returns:** type void

### setColor(com.bitwig.extension.api.Color)

Tries to set this light's state to be the best state to represent the supplied Color. For this
 to be used you must first call 

invalid reference
#setColorToStateFunction(IntFunction)

.

**Parameters:**
- `color` (type Color)

**Returns:** type void

### setColorSupplier(java.util.function.Supplier)

Tries to set this light's state to be the best state to represent the value supplied by the
 Supplier. For this to be used you must first call 

invalid reference
#setColorToStateFunction(IntFunction)

.

**Parameters:**
- `colorSupplier` (type Supplier<Color>)

**Returns:** type void

### getBestLightStateForColor(com.bitwig.extension.api.Color)

Determines the best light state for the supplied color. For this to be used you must first call
 

invalid reference
#setColorToStateFunction(IntFunction)

.

**Parameters:**
- `color` (type Color)

**Returns:** type InternalHardwareLightState

### onUpdateHardware(java.lang.Runnable)

Sets an optional callback for this element whenever it's state needs to be sent to the hardware. This
 will be called when calling HardwareSurface.updateHardware() if the state needs to be sent.

**Parameters:**
- `sendStateRunnable` (type Runnable)

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

