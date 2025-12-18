# IntegerHardwareProperty

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents an output value shown on some hardware.

**Extends:** `com.bitwig.extension.controller.api.HardwareProperty`

## Methods

### currentValue()

Gets the current value. This is the value that should be sent to the hardware to be displayed.

**Returns:** type int

### lastSentValue()

The value that was last sent to the hardware.

**Returns:** type int

### onUpdateHardware(java.util.function.IntConsumer)

Specifies a callback that should be called with the value that needs to be sent to the hardware. This
 callback is called as a result of calling the HardwareSurface.updateHardware() method (typically
 from the flush method).

**Parameters:**
- `sendValueConsumer` (type IntConsumer)

**Returns:** type void

### setValue(int)

Sets the current value.

**Parameters:**
- `value` (type int)

**Returns:** type void

### setValueSupplier(java.util.function.IntSupplier)

Sets the current value from a BooleanSupplier that supplies the latest value.

**Parameters:**
- `supplier` (type IntSupplier)

**Returns:** type void

