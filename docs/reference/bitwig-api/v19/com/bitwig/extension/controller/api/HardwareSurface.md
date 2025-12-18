# HardwareSurface

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents a surface that can contain HardwareElements such as HardwareButtons,
 HardwareSliders, MultiStateHardwareLights etc

 
 This information allows Bitwig Studio to construct a reliable physical model of the hardware. This
 information can be used to simulate hardware being present even when physical hardware is not available
 (and may also be used for other purposes in the future).

 
 To be able to simulate hardware being connected so that you can debug controllers without the real hardware
 you need to do the following:

 
 Create a file with the name "config.json" in your user settings directory. The location of this directory
 is platform dependent:

 
 On Windows: %LOCALAPPDATA%\Bitwig Studio
 On macOS: Library/Application Support/Bitwig/Bitwig Studio
 On Linux: $HOME/.BitwigStudio
 

 
 Then add the following line to the config.json file:

  extension-dev : true
 

 
 You will then need to restart Bitwig Studio. To simulate the controller being connected you can right click
 on the controller in the preferences and select "Simulate device connected".

 
 If you have also provided physical positions for various HardwareElements using
 HardwareElement.setBounds(double, double, double, double) then you can also see a GUI simulator for
 your controller by selecting "Show simulated hardware GUI".

## Methods

### createHardwareSlider(java.lang.String)

Creates a HardwareSlider that represents a physical slider on a controller.

**Parameters:**
- `id` (type String): A unique string that identifies this control.

**Returns:** type HardwareSlider

### createHardwareSlider(java.lang.String,double)

Creates a HardwareSlider that represents a physical slider on a controller.

**Parameters:**
- `id` (type String): A unique string that identifies this control.
- `currentValue` (type double): The current position of the slider 0..1

**Returns:** type HardwareSlider

### createAbsoluteHardwareKnob(java.lang.String)

Creates an AbsoluteHardwareKnob that represents a physical knob on a controller that can be used
 to input an absolute value.

**Parameters:**
- `id` (type String): A unique string that identifies this control.

**Returns:** type AbsoluteHardwareKnob

### createAbsoluteHardwareKnob(java.lang.String,double)

Creates an AbsoluteHardwareKnob that represents a physical knob on a controller that can be used
 to input an absolute value.

**Parameters:**
- `id` (type String): A unique string that identifies this control.
- `currentValue` (type double): The current position of the knob 0..1

**Returns:** type AbsoluteHardwareKnob

### createRelativeHardwareKnob(java.lang.String)

Creates an RelativeHardwareKnob that represents a physical knob on a controller that can be used
 to input a relative value change.

**Parameters:**
- `id` (type String): A unique string that identifies this control.

**Returns:** type RelativeHardwareKnob

### createPianoKeyboard(java.lang.String,int,int,int)

**Parameters:**
- `id` (type String)
- `numKeys` (type int)
- `octave` (type int)
- `startKeyInOctave` (type int)

**Returns:** type PianoKeyboard

### createHardwareButton(java.lang.String)

Creates a HardwareButton that represents a physical button on a controller

**Parameters:**
- `id` (type String): A unique string that identifies this control.

**Returns:** type HardwareButton

### createOnOffHardwareLight(java.lang.String)

Creates a OnOffHardwareLight that represents a physical light on a controller

**Parameters:**
- `id` (type String)

**Returns:** type OnOffHardwareLight

### createMultiStateHardwareLight(java.lang.String)

Creates a MultiStateHardwareLight that represents a physical light on a controller

**Parameters:**
- `id` (type String): A unique string that identifies this parameter.

**Returns:** type MultiStateHardwareLight

### createHardwareTextDisplay(java.lang.String,int)

Creates a HardwareTextDisplay that represents a physical text display on a controller

**Parameters:**
- `id` (type String): A unique string that identifies this control.
- `numLines` (type int)

**Returns:** type HardwareTextDisplay

### createHardwarePixelDisplay(java.lang.String,com.bitwig.extension.api.graphics.Bitmap)

Creates a HardwarePixelDisplay that displays the provided Bitmap that is rendered by the
 controller.

**Parameters:**
- `id` (type String)
- `bitmap` (type Bitmap)

**Returns:** type HardwarePixelDisplay

### setPhysicalSize(double,double)

Sets the physical size of this controller in mm.

**Parameters:**
- `widthInMM` (type double)
- `heightInMM` (type double)

**Returns:** type void

### updateHardware()

Updates the state of all HardwareOutputElements that have changed since the last time this
 method was called.

 Any onUpdateHardware callbacks that have been registered on HardwareOutputElements or
 HardwarePropertys will be invoked if their state/value has changed since the last time it was
 called.

 This is typically called by the control script from its flush method.

**Returns:** type void

### invalidateHardwareOutputState()

Mark all HardwareOutputElements as needing to resend their output state, regardless of it has
 changed or not.

**Returns:** type void

### hardwareControls()

A list of all the HardwareControls that have been created on this HardwareSurface.

**Returns:** type List<? extends HardwareControl>

### hardwareElementWithId(java.lang.String)

Finds the HardwareElement that has the supplied id or null if not found.

**Parameters:**
- `id` (type String)

**Returns:** type HardwareElement

### hardwareOutputElements()

List of all HardwareElements on this HardwareSurface.

**Returns:** type List<? extends HardwareOutputElement>

