# AbsoluteHardwarControlBindable

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Something that can be bound to an AbsoluteHardwareControl and can respond to the user input (such
 as user moving a slider up or down) in a meaningful way.

**Extends:** `com.bitwig.extension.controller.api.HardwareBindable`

## Methods

### addBinding(com.bitwig.extension.controller.api.AbsoluteHardwareControl)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type AbsoluteHardwareControl)

**Returns:** type AbsoluteHardwareControlBinding - The newly created binding

### addBindingWithRange(com.bitwig.extension.controller.api.AbsoluteHardwareControl,double,double)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way. This target will be adjusted within the supplied normalized
 range.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type AbsoluteHardwareControl)
- `minNormalizedValue` (type double)
- `maxNormalizedValue` (type double)

**Returns:** type AbsoluteHardwareControlBinding - The newly created binding

