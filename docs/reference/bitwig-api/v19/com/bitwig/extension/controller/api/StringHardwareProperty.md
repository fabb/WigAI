# StringHardwareProperty

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents an output value shown on some hardware (for example, the title of a track).

**Extends:** `com.bitwig.extension.controller.api.HardwareProperty`

## Methods

### currentValue()

Gets the current value. This is the value that should be sent to the hardware to be displayed.

**Returns:** type String

### lastSentValue()

The value that was last sent to the hardware.

**Returns:** type String

### onUpdateHardware(java.util.function.Consumer)

Specifies a callback that should be called with the value that needs to be sent to the hardware. This
 callback is called as a result of calling the HardwareSurface.updateHardware() method (typically
 from the flush method).

**Parameters:**
- `sendValueConsumer` (type Consumer<String>)

**Returns:** type void

### setValue(java.lang.String)

Sets the current value.

**Parameters:**
- `value` (type String)

**Returns:** type void

### setValueSupplier(java.util.function.Supplier)

Sets the current value from a Supplier that supplies the latest value.

**Parameters:**
- `supplier` (type Supplier<String>)

**Returns:** type void

### getMaxChars()

The maximum number of characters that can be output or -1 if not specified and there is no limit.

**Returns:** type int

### setMaxChars(int)

**Parameters:**
- `maxChars` (type int)

**Returns:** type void

