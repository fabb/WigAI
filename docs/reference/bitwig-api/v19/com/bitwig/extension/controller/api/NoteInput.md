# NoteInput

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface implement note input functionality used for recording notes in Bitwig Studio
 and for playing the instruments in tracks on hardware keyboards. In Bitwig Studio the note inputs are shown
 in the input choosers of Bitwig Studio tracks.

## Methods

### setShouldConsumeEvents(boolean)

Specifies if the note input should consume MIDI notes, or in other words if it should prevent forwarding
 incoming notes to the MIDI callback registered in MidiIn.setMidiCallback(com.bitwig.extension.callback.ShortMidiDataReceivedCallback). This setting is `true`
 by default.

**Parameters:**
- `shouldConsumeEvents` (type boolean): `true` if note events should be consumed, `false` of the events should be additionally sent to
           the callback registered via

**Returns:** type void

### setKeyTranslationTable(java.lang.Object[])

Specifies a translation table which defines the actual key value (0-127) of notes arriving in Bitwig
 Studio for each note key potentially received from the hardware. This is used for note-on/off and
 polyphonic aftertouch events. Specifying a value of `-1` for a key means that notes with the key value
 will be filtered out.

 Typically this method is used to implement transposition or scale features in controller scripts. By
 default an identity transform table is configured, which means that all incoming MIDI notes keep their
 original key value when being sent into Bitwig Studio.

**Parameters:**
- `table` (type Object[]): an array which should contain 128 entries. Each entry should be a note value in the range
           [0..127] or -1 in case of filtering.

**Returns:** type void

### setVelocityTranslationTable(java.lang.Object[])

Specifies a translation table which defines the actual velocity value (0-127) of notes arriving in
 Bitwig Studio for each note velocity potentially received from the hardware. This is used for note-on
 events only.

 Typically this method is used to implement velocity curves or fixed velocity mappings in controller
 scripts. By default an identity transform table is configured, which means that all incoming MIDI notes
 keep their original velocity when being sent into Bitwig Studio.

**Parameters:**
- `table` (type Object[]): an array which should contain 128 entries. Each entry should be a note value in the range
           [0..127] or -1 in case of filtering.

**Returns:** type void

### assignPolyphonicAftertouchToExpression(int,com.bitwig.extension.controller.api.NoteInput.NoteExpression,int)

Assigns polyphonic aftertouch MIDI messages to the specified note expression. Multi-dimensional control
 is possible by calling this method several times with different MIDI channel parameters. If a key
 translation table is configured by calling setKeyTranslationTable(java.lang.Object[]), that table is used for
 polyphonic aftertouch as well.

**Parameters:**
- `channel` (type int): the MIDI channel to map, range [0..15]
- `expression` (type NoteInput.NoteExpression): the note-expression to map for the given MIDI channel
- `pitchRange` (type int): the pitch mapping range in semitones, values must be in the range [1..24]. This parameter is
           ignored for non-pitch expressions.

**Returns:** type void

### setUseExpressiveMidi(boolean,int,int)

Enables use of Expressive MIDI mode. (note-per-channel)

**Parameters:**
- `useExpressiveMidi` (type boolean): enabled/disable the MPE mode for this note-input
- `baseChannel` (type int): which channel (must be either 0 or 15) which is used as the base for this note-input
- `pitchBendRange` (type int): initial pitch bend range used

**Returns:** type void

### setUseMultidimensionalPolyphonicExpression(boolean,int)

Enables use of Multidimensional Polyphonic Expression mode. (note-per-channel)

**Parameters:**
- `useMPE` (type boolean): enabled/disable the MPE mode for this note-input
- `baseChannel` (type int): which channel (must be either 0 or 15) which is used as the base for this note-input

**Returns:** type void

### sendRawMidiEvent(int,int,int)

Sends MIDI data directly to the note input. This will bypass both the event filter and translation
 tables. The MIDI channel of the message will be ignored.

**Parameters:**
- `status` (type int): the status byte of the MIDI message
- `data0` (type int): the data0 part of the MIDI message
- `data1` (type int): the data1 part of the MIDI message

**Returns:** type void

### noteLatch()

Creates a proxy object to the NoteInput's NoteLatch component.

**Returns:** type NoteLatch

### arpeggiator()

Creates a proxy object to the NoteInput's Arpeggiator component.

**Returns:** type Arpeggiator

### includeInAllInputs()

Should this note input be included in the "All Inputs" note source?

**Returns:** type SettableBooleanValue

