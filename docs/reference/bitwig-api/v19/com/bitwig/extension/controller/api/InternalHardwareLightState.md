# InternalHardwareLightState

- Kind: abstract class
- Package: `com.bitwig.extension.controller.api`

Defines the current state of a MultiStateHardwareLight. What this state means is entirely up to the
 controller implementation.

 The Object.equals(Object) method MUST be overridden to compare light states correctly.

**Extends:** java.lang.Object

## Methods

### getVisualState()

The visual state of this light (used by Bitwig Studio to visualize the light when needed).

**Returns:** type HardwareLightVisualState

### equals(java.lang.Object)

**Parameters:**
- `obj` (type Object)

**Returns:** type boolean

