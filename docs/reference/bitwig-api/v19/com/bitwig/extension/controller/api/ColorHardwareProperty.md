# ColorHardwareProperty

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents an output value shown on some hardware (for example, the color of a light).

**Extends:** `com.bitwig.extension.controller.api.HardwareProperty`

## Methods

### currentValue()

Gets the current value. This is the value that should be sent to the hardware to be displayed.

**Returns:** type Color

### lastSentValue()

The value that was last sent to the hardware.

**Returns:** type Color

### onUpdateHardware(java.util.function.Consumer)

Specifies a callback that should be called with the value that needs to be sent to the hardware. This
 callback is called as a result of calling the HardwareSurface.updateHardware() method (typically
 from the flush method).

**Parameters:**
- `sendValueConsumer` (type Consumer<Color>)

**Returns:** type void

### setValue(com.bitwig.extension.api.Color)

Sets the current value.

**Parameters:**
- `value` (type Color)

**Returns:** type void

### setValueSupplier(java.util.function.Supplier)

Sets the current value from a Supplier that supplies the latest value.

**Parameters:**
- `supplier` (type Supplier<Color>)

**Returns:** type void

