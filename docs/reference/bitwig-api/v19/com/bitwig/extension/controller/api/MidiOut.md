# MidiOut

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface are used to send MIDI messages to a specific MIDI hardware.

## Methods

### sendMidi(int,int,int)

Sends a MIDI message to the hardware device.

**Parameters:**
- `status` (type int): the status byte of the MIDI message, system messages are not permitted.
- `data1` (type int): the data1 part of the MIDI message
- `data2` (type int): the data2 part of the MIDI message

**Returns:** type void

### sendSysex(java.lang.String)

Sends a MIDI SysEx message to the hardware device.

 Starting from API version 19, sending invalid sysex will crash the ControllerExtension.

**Parameters:**
- `hexString` (type String): the sysex message formatted as hexadecimal value string

**Returns:** type void

### sendSysex(byte[])

Sends a MIDI SysEx message to the hardware device.

 Starting from API version 19, sending invalid sysex will crash the ControllerExtension.

**Parameters:**
- `data` (type byte[]): the array of bytes to send

**Returns:** type void

### sendSysexBytes(byte[])

Sends a MIDI SysEx message to the hardware device. This method is identical to sendSysex(byte[])
 but exists so that Javascript controllers can explicitly call this method instead of relying on some
 intelligent overload resolution of the Javascript engine based on its loose type system.

 Starting from API version 19, sending invalid sysex will crash the ControllerExtension.

**Parameters:**
- `data` (type byte[]): the array of bytes to send

**Returns:** type void

### setShouldSendMidiBeatClock(boolean)

Enables or disables sending MIDI beat clock messages to the hardware depending on the given parameter.
 Typically MIDI devices that run an internal sequencer such as hardware step sequencers would be
 interested in MIDI clock messages.

**Parameters:**
- `shouldSendClock` (type boolean): `true` in case the hardware should receive MIDI clock messages, `false` otherwise

**Returns:** type void

