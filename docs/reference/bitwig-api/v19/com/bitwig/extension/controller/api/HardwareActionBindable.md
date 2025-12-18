# HardwareActionBindable

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Something that can be bound to a hardware action (such as user pressing a button).

**Extends:** `com.bitwig.extension.controller.api.HardwareBindable`

## Methods

### addBinding(com.bitwig.extension.controller.api.HardwareAction)

Binds this target to the supplied HardwareAction so that when the hardware action occurs this
 target is invoked.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `action` (type HardwareAction)

**Returns:** type HardwareActionBinding

### invoke()

Invokes the action.

**Returns:** type void

