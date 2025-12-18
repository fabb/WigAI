# HardwareBindingWithRange

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents a binding from some hardware input to a ranged value.

**Extends:** `com.bitwig.extension.controller.api.HardwareBinding`

## Methods

### setMinNormalizedValue(double)

Sets the minimum normalized value (0...1) that should be used for this binding.

**Parameters:**
- `min` (type double)

**Returns:** type void

### setMaxNormalizedValue(double)

Sets the maximum normalized value (0...1) that should be used for this binding.

**Parameters:**
- `max` (type double)

**Returns:** type void

### setNormalizedRange(double,double)

Sets the normalized range (0...1) that should be used for this binding.

**Parameters:**
- `min` (type double)
- `max` (type double)

**Returns:** type void

### removeBinding()

Removes this binding between its source and target so it is no longer in effect.

**Returns:** type void

