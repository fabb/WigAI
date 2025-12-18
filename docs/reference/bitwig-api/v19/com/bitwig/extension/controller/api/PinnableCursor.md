# PinnableCursor

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Interface that defines a cursor that can be "pinned".

**Extends:** `com.bitwig.extension.controller.api.Cursor`

## Methods

### isPinned()

Determines if this cursor is currently pinned or not. If the cursor is pinned then it won't follow the
 selection the user makes in the application.

**Returns:** type SettableBooleanValue

### selectPrevious()

Select the previous item.

**Returns:** type void

### selectPreviousAction()

**Returns:** type HardwareActionBindable

### selectNext()

Select the next item.

**Returns:** type void

### selectNextAction()

**Returns:** type HardwareActionBindable

### selectFirst()

Select the first item.

**Returns:** type void

### selectLast()

Select the last item.

**Returns:** type void

### hasNext()

Boolean value that reports whether there is an item after the current cursor position.

**Returns:** type BooleanValue

### hasPrevious()

Boolean value that reports whether there is an item before the current cursor position.

**Returns:** type BooleanValue

### addCanSelectPreviousObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers a function with bool argument that gets called when the previous item gains or remains
 selectable.

**Parameters:**
- `callback` (type BooleanValueChangedCallback)

**Returns:** type void

### addCanSelectNextObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers a function with bool argument that gets called when the next item gains or remains
 selectable.

**Parameters:**
- `callback` (type BooleanValueChangedCallback)

**Returns:** type void

### addBinding(com.bitwig.extension.controller.api.RelativeHardwareControl)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)

**Returns:** type RelativeHardwareControlBinding

### addBindingWithSensitivity(com.bitwig.extension.controller.api.RelativeHardwareControl,double)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)
- `sensitivity` (type double)

**Returns:** type RelativeHardwareControlBinding

