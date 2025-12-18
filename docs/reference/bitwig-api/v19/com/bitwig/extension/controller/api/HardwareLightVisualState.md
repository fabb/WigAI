# HardwareLightVisualState

- Kind: class
- Package: `com.bitwig.extension.controller.api`

Defines the visual state of a hardware light so that it can be visualized in Bitwig Studio's user
 interface.

 This is currently only used when simulating hardware when it is not present for debugging but it may be
 used for other purposes in the future.

**Extends:** java.lang.Object

## Methods

### createForColor(com.bitwig.extension.api.Color)

**Parameters:**
- `color` (type Color)

**Returns:** type HardwareLightVisualState

### createForColor(com.bitwig.extension.api.Color,com.bitwig.extension.api.Color)

**Parameters:**
- `color` (type Color)
- `labelColor` (type Color)

**Returns:** type HardwareLightVisualState

### createBlinking(com.bitwig.extension.api.Color,com.bitwig.extension.api.Color,double,double)

**Parameters:**
- `onColor` (type Color)
- `offColor` (type Color)
- `onBlinkTimeInSec` (type double)
- `offBlinkTimeInSec` (type double)

**Returns:** type HardwareLightVisualState

### createBlinking(com.bitwig.extension.api.Color,com.bitwig.extension.api.Color,com.bitwig.extension.api.Color,com.bitwig.extension.api.Color,double,double)

**Parameters:**
- `onColor` (type Color)
- `offColor` (type Color)
- `labelOnColor` (type Color)
- `labelOffColor` (type Color)
- `onBlinkTimeInSec` (type double)
- `offBlinkTimeInSec` (type double)

**Returns:** type HardwareLightVisualState

### isBlinking()

**Returns:** type boolean

### getColor()

**Returns:** type Color

### getBlinkOffColor()

**Returns:** type Color

### getOffBlinkTime()

**Returns:** type double

### getOnBlinkTime()

**Returns:** type double

### getLabelColor()

**Returns:** type Color

### getLabelBlinkOffColor()

**Returns:** type Color

### hashCode()

**Returns:** type int

### equals(java.lang.Object)

**Parameters:**
- `obj` (type Object)

**Returns:** type boolean

