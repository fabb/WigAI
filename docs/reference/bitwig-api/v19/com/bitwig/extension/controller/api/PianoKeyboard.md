# PianoKeyboard

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents a physical piano keyboard on a HardwareSurface.

**Extends:** `com.bitwig.extension.controller.api.HardwareElement`

## Methods

### setMidiIn(com.bitwig.extension.controller.api.MidiIn)

The MidiIn where this piano keyboard would send key presses. If set this allows the simulator
 for the hardware to simulate the note input.

**Parameters:**
- `midiIn` (type MidiIn)

**Returns:** type void

### setNoteInput(com.bitwig.extension.controller.api.NoteInput)

Sets the NoteInput that this keyboard should send notes to.

**Parameters:**
- `noteInput` (type NoteInput)

**Returns:** type void

### setChannel(int)

**Parameters:**
- `channel` (type int)

**Returns:** type void

### setIsVelocitySensitive(boolean)

**Parameters:**
- `value` (type boolean)

**Returns:** type void

### setSupportsPolyAftertouch(boolean)

**Parameters:**
- `value` (type boolean)

**Returns:** type void

### getId()

The unique id associated with this element.

**Returns:** type String

### getLabel()

An optional label associated with this element.

**Returns:** type String

### setLabel(java.lang.String)

Sets the label for this hardware control as written on the hardware.

**Parameters:**
- `label` (type String)

**Returns:** type void

### getLabelColor()

The color of the label.

**Returns:** type Color

### setLabelColor(com.bitwig.extension.api.Color)

Sets the color of the label.

**Parameters:**
- `color` (type Color)

**Returns:** type void

### getLabelPosition()

RelativePosition that defines where the label is.

**Returns:** type RelativePosition

### setLabelPosition(com.bitwig.extension.controller.api.RelativePosition)

**Parameters:**
- `position` (type RelativePosition)

**Returns:** type void

### setBounds(double,double,double,double)

The physical bounds of this hardware element on the controller.

**Parameters:**
- `xInMM` (type double)
- `yInMM` (type double)
- `widthInMM` (type double)
- `heightInMM` (type double)

**Returns:** type void

### getX()

**Returns:** type double

### getY()

**Returns:** type double

### getWidth()

**Returns:** type double

### getHeight()

**Returns:** type double

