# HardwareBindingSource

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents the source of a HardwareBinding.

## Methods

### canBindTo(java.lang.Object)

Checks if it is possible to make a binding from this source to the supplied target object.

**Parameters:**
- `target` (type Object)

**Returns:** type boolean

### addBinding(com.bitwig.extension.controller.api.HardwareBindable)

Binds this source to the supplied target and returns the created binding. This can only be called if the
 canBindTo(Object) returns true.

**Parameters:**
- `target` (type HardwareBindable)

**Returns:** type HardwareBindingType

### clearBindings()

Clears all bindings from this source to its targets.

**Returns:** type void

### setBinding(com.bitwig.extension.controller.api.HardwareBindable)

Ensures there is a single binding to the supplied target.

 This is a convenience method that is equivalent to calling clearBindings() and the
 addBinding(HardwareBindable)

**Parameters:**
- `target` (type HardwareBindable)

**Returns:** type HardwareBindingType

