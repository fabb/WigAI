# ObjectHardwareProperty

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents an output value shown on some hardware.

**Extends:** `com.bitwig.extension.controller.api.HardwareProperty`

## Methods

### currentValue()

Gets the current value. This is the value that should be sent to the hardware to be displayed.

**Returns:** type T

### lastSentValue()

The value that was last sent to the hardware.

**Returns:** type T

### onUpdateHardware(java.util.function.Consumer)

Specifies a callback that should be called with the value that needs to be sent to the hardware. This
 callback is called as a result of calling the HardwareSurface.updateHardware() method (typically
 from the flush method).

**Parameters:**
- `sendValueConsumer` (type Consumer<? extends T>)

**Returns:** type void

### setValue(T)

Sets the current value.

**Parameters:**
- `value` (type T)

**Returns:** type void

### setValueSupplier(java.util.function.Supplier)

Sets the current value from a BooleanSupplier that supplies the latest value.

**Parameters:**
- `supplier` (type Supplier<? extends T>)

**Returns:** type void

