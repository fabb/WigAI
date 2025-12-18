# BooleanHardwareProperty

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents an output value shown on some hardware (for example, if an LED is on or off).

**Extends:** `com.bitwig.extension.controller.api.HardwareProperty`

## Methods

### currentValue()

Gets the current value. This is the value that should be sent to the hardware to be displayed.

**Returns:** type boolean

### lastSentValue()

The value that was last sent to the hardware.

**Returns:** type boolean

### onUpdateHardware(java.util.function.Consumer)

Specifies a callback that should be called with the value that needs to be sent to the hardware. This
 callback is called as a result of calling the HardwareSurface.updateHardware() method (typically
 from the flush method).

**Parameters:**
- `sendValueConsumer` (type Consumer<Boolean>)

**Returns:** type void

### setValue(boolean)

Sets the current value.

**Parameters:**
- `value` (type boolean)

**Returns:** type void

### setValueSupplier(java.util.function.BooleanSupplier)

Sets the current value from a BooleanSupplier that supplies the latest value.

**Parameters:**
- `supplier` (type BooleanSupplier)

**Returns:** type void

