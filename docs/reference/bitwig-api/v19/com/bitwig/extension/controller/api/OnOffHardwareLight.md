# OnOffHardwareLight

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Defines a simple hardware light that only has an on and off state.

**Extends:** `com.bitwig.extension.controller.api.HardwareLight`

## Methods

### isOn()

Property that determines if this light is on or not.

**Returns:** type BooleanHardwareProperty

### setOnColor(com.bitwig.extension.api.Color)

**Parameters:**
- `color` (type Color)

**Returns:** type void

### setOffColor(com.bitwig.extension.api.Color)

**Parameters:**
- `color` (type Color)

**Returns:** type void

### setOnVisualState(com.bitwig.extension.controller.api.HardwareLightVisualState)

**Parameters:**
- `state` (type HardwareLightVisualState)

**Returns:** type void

### setOffVisualState(com.bitwig.extension.controller.api.HardwareLightVisualState)

**Parameters:**
- `state` (type HardwareLightVisualState)

**Returns:** type void

### setStateToVisualStateFuncation(java.util.function.Function)

**Parameters:**
- `function` (type Function<Boolean,HardwareLightVisualState>)

**Returns:** type void

### setStateToVisualStateFunction(java.util.function.Function)

**Parameters:**
- `function` (type Function<Boolean,HardwareLightVisualState>)

**Returns:** type void

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

