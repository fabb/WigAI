# ShortMidiMessageReceivedCallback

- Kind: interface
- Package: `com.bitwig.extension.callback`

**Extends:** `com.bitwig.extension.callback.ShortMidiDataReceivedCallback`

## Methods

### midiReceived(com.bitwig.extension.api.util.midi.ShortMidiMessage)

Callback for receiving short (normal) MIDI messages on this MIDI input port.

**Parameters:**
- `msg` (type ShortMidiMessage)

**Returns:** type void

### midiReceived(int,int,int)

Callback for receiving short (normal) MIDI messages on this MIDI input port.

**Parameters:**
- `statusByte` (type int)
- `data1` (type int)
- `data2` (type int)

**Returns:** type void

