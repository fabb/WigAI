# Scrollable

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Interface for something that can be scrolled.

**Extends:** `com.bitwig.extension.controller.api.RelativeHardwarControlBindable`

## Methods

### scrollPosition()

Value that reports the current scroll position.

**Returns:** type SettableIntegerValue

### scrollIntoView(int)

Scrolls the supplied position into view if it isn't already.

**Parameters:**
- `position` (type int)

**Returns:** type void

### scrollBy(int)

Scrolls by a number of steps.

**Parameters:**
- `amount` (type int): The number of steps to scroll by (positive is forwards and negative is backwards).

**Returns:** type void

### scrollForwards()

Scrolls forwards by one step. This is the same as calling scrollBy(int) with 1

**Returns:** type void

### scrollForwardsAction()

**Returns:** type HardwareActionBindable

### scrollBackwards()

Scrolls forwards by one step. This is the same as calling scrollBy(int) with -1

**Returns:** type void

### scrollBackwardsAction()

**Returns:** type HardwareActionBindable

### scrollByPages(int)

Scrolls by a number of pages.

**Parameters:**
- `amount` (type int): The number of pages to scroll by (positive is forwards and negative is backwards).

**Returns:** type void

### scrollPageForwards()

Scrolls forwards by one page.

**Returns:** type void

### scrollPageForwardsAction()

**Returns:** type HardwareActionBindable

### scrollPageBackwards()

Scrolls backwards by one page.

**Returns:** type void

### scrollPageBackwardsAction()

**Returns:** type HardwareActionBindable

### canScrollBackwards()

Value that reports if it is possible to scroll the bank backwards or not.

**Returns:** type BooleanValue

### canScrollForwards()

Value that reports if it is possible to scroll the bank forwards or not.

**Returns:** type BooleanValue

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

