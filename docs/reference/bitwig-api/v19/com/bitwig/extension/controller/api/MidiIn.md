# MidiIn

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface are used to setup handler functions for incoming MIDI messages from a specific
 MIDI hardware.

 
 Expressions can be used to generate matchers for various MIDI events that can then be used to update
 hardware control states (see createActionMatcher(String) and HardwareControl).
 

 
 The expression language supports these operators in the same way that C, Java, C++ do:
 +, -, *, /, %, <<, >>, &&, ||, &, |, ^, <, <=, >, >=, ==, !=
 

 The following variables are also defined for matching parts of the event:
 
 status - Value of the status byte
 data1 - Value of the first data byte
 data2 - Value of the second data byte
 event - Integer value of the whole MIDI event with data2 byte in the least significant bits
 

 
 Integers can be represented in hex using same syntax as C. 'true' and 'false' keywords are also defined.
 

## Methods

### setMidiCallback(com.bitwig.extension.callback.ShortMidiDataReceivedCallback)

Registers a callback for receiving short (normal) MIDI messages on this MIDI input port.

**Parameters:**
- `callback` (type ShortMidiDataReceivedCallback): a callback function that receives three integer parameters: 1. the status byte 2. the data1
           value 2. the data2 value

**Returns:** type void

### setSysexCallback(com.bitwig.extension.callback.SysexMidiDataReceivedCallback)

Registers a callback for receiving sysex MIDI messages on this MIDI input port.

**Parameters:**
- `callback` (type SysexMidiDataReceivedCallback): a callback function that takes a single string argument

**Returns:** type void

### createNoteInput(java.lang.String,java.lang.String...)

Creates a note input that appears in the track input choosers in Bitwig Studio. This method must be
 called within the `init()` function of the script. The messages matching the given mask parameter will
 be fed directly to the application, and are not processed by the script.

**Parameters:**
- `name` (type String): the name of the note input as it appears in the track input choosers in Bitwig Studio
- `masks` (type String...): a filter string formatted as hexadecimal value with `?` as wildcard. For example `80????`
           would match note-off on channel 1 (0). When this parameter is null, a standard filter will
           be used to forward note-related messages on channel 1 (0).

           If multiple note input match the same MIDI event then they'll all receive the MIDI event, and
           if one of them does not consume events then the events won't be consumed.

**Returns:** type NoteInput - the object representing the requested note input

### createAbsoluteCCValueMatcher(int,int)

Creates a matcher that matches the absolute value of a MIDI CC message.

**Parameters:**
- `channel` (type int)
- `controlNumber` (type int)

**Returns:** type AbsoluteHardwareValueMatcher

### createAbsoluteCCValueMatcher(int)

Creates a matcher that matches the absolute value of a MIDI CC message regardless of its channel.

**Parameters:**
- `controlNumber` (type int)

**Returns:** type AbsoluteHardwareValueMatcher

### createPolyAftertouchValueMatcher(int,int)

Creates a matcher that matches the absolute value of a Poly AT message.

**Parameters:**
- `channel` (type int)
- `note` (type int)

**Returns:** type AbsoluteHardwareValueMatcher

### createRelativeSignedBitCCValueMatcher(int,int,int)

Creates a matcher that matches the relative value of a MIDI CC message encoded using signed bit.

**Parameters:**
- `channel` (type int)
- `controlNumber` (type int)
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createRelativeSignedBit2CCValueMatcher(int,int,int)

Creates a matcher that matches the relative value of a MIDI CC message encoded using signed bit 2.

**Parameters:**
- `channel` (type int)
- `controlNumber` (type int)
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createRelativeBinOffsetCCValueMatcher(int,int,int)

Creates a matcher that matches the relative value of a MIDI CC message encoded using bin offset.

**Parameters:**
- `channel` (type int)
- `controlNumber` (type int)
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createRelative2sComplementCCValueMatcher(int,int,int)

Creates a matcher that matches the relative value of a MIDI CC message encoded using 2s complement.

**Parameters:**
- `channel` (type int)
- `controlNumber` (type int)
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createAbsolutePitchBendValueMatcher(int)

Create a matcher that matches the absolute value of a MIDI pitch bend message.

**Parameters:**
- `channel` (type int)

**Returns:** type AbsoluteHardwareValueMatcher

### createSequencedValueMatcher(com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher,com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher,boolean)

Creates an absolute value matcher that is defined by 2 separate MIDI events that have to occur in
 sequence.

 This can be used to get a much higher precision value that a single MIDI event would allow. Some
 controllers for example will send 2 CC events for a single value.

**Parameters:**
- `firstValueMatcher` (type AbsoluteHardwareValueMatcher)
- `secondValueMatcher` (type AbsoluteHardwareValueMatcher)
- `areMostSignificantBitsInSecondEvent` (type boolean)

**Returns:** type AbsoluteHardwareValueMatcher

### createAbsoluteValueMatcher(java.lang.String,java.lang.String,int)

Creates a matcher that matches the absolute value of a MIDI CC message by using expressions to filter
 and extract a value out of the MIDI event.

**Parameters:**
- `eventExpression` (type String): Expression that must be true in order to extract the value.
- `valueExpression` (type String): Expression that determines the value once an event has been matched.
- `valueBitCount` (type int): The number of bits that are relevant from the value extracted by the valueExpression.

**Returns:** type AbsoluteHardwareValueMatcher

### createRelativeValueMatcher(java.lang.String,double)

Creates a matcher that applies a relative adjustment when a MIDI event occurs matching an expression.

**Parameters:**
- `eventExpression` (type String): Expression that must be true in order to extract the value.
- `relativeAdjustment` (type double): The amount of relative adjustment that should be applied

**Returns:** type RelativeHardwareValueMatcher

### createRelativeSignedBitValueMatcher(java.lang.String,java.lang.String,int,int)

Creates a matcher that matches the relative value (encoded as signed bit) of a MIDI CC message by using
 expressions to filter and extract a value out of the MIDI event.

**Parameters:**
- `eventExpression` (type String): Expression that must be true in order to extract the value.
- `valueExpression` (type String): Expression that determines the value once an event has been matched.
- `valueBitCount` (type int): The number of bits that are relevant from the value extracted by the valueExpression.
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createRelativeSignedBitValueMatcher(com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher,int)

Creates a matcher that converts a value matched by an AbsoluteHardwareValueMatcher to a relative
 value using signed bit.

**Parameters:**
- `valueMatcher` (type AbsoluteHardwareValueMatcher): Value matcher that matches the value that needs to be converted to a relative value
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createRelativeSignedBit2ValueMatcher(java.lang.String,java.lang.String,int,int)

Creates a matcher that matches the relative value (encoded as signed bit 2) of a MIDI CC message by
 using expressions to filter and extract a value out of the MIDI event.

**Parameters:**
- `eventExpression` (type String): Expression that must be true in order to extract the value.
- `valueExpression` (type String): Expression that determines the value once an event has been matched.
- `valueBitCount` (type int): The number of bits that are relevant from the value extracted by the valueExpression.
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createRelativeSignedBit2ValueMatcher(com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher,int)

Creates a matcher that converts a value matched by an AbsoluteHardwareValueMatcher to a relative
 value using signed bit 2.

**Parameters:**
- `valueMatcher` (type AbsoluteHardwareValueMatcher)
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createRelativeBinOffsetValueMatcher(java.lang.String,java.lang.String,int,int)

Creates a matcher that matches the relative value (encoded as bin offset) of a MIDI CC message by using
 expressions to filter and extract a value out of the MIDI event.

**Parameters:**
- `eventExpression` (type String): Expression that must be true in order to extract the value.
- `valueExpression` (type String): Expression that determines the value once an event has been matched.
- `valueBitCount` (type int): The number of bits that are relevant from the value extracted by the valueExpression.
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createRelativeBinOffsetValueMatcher(com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher,int)

Creates a matcher that converts a value matched by an AbsoluteHardwareValueMatcher to a relative
 value using bin offset.

**Parameters:**
- `valueMatcher` (type AbsoluteHardwareValueMatcher)
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createRelative2sComplementValueMatcher(java.lang.String,java.lang.String,int,int)

Creates a matcher that matches the relative value (encoded as 2s complement) of a MIDI CC message by
 using expressions to filter and extract a value out of the MIDI event.

**Parameters:**
- `eventExpression` (type String): Expression that must be true in order to extract the value.
- `valueExpression` (type String): Expression that determines the value once an event has been matched.
- `valueBitCount` (type int): The number of bits that are relevant from the value extracted by the valueExpression.
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createRelative2sComplementValueMatcher(com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher,int)

Creates a matcher that converts a value matched by an AbsoluteHardwareValueMatcher to a relative
 value using 2s complement.

**Parameters:**
- `valueMatcher` (type AbsoluteHardwareValueMatcher)
- `valueAmountForOneFullRotation` (type int): The value that would represent one full rotation to the right (should be very similar to the
           amount of rotation needed to take an absolute knob from 0 to 1). For example, if a value of
           127 meant it had been rotated to the right by a full rotation then you would pass 127 here.
           This ensures that

**Returns:** type RelativeHardwareValueMatcher

### createCCActionMatcher(int,int,int)

Creates a matcher that recognizes an action when getting a MIDI CC event with a specific value.

**Parameters:**
- `channel` (type int)
- `controlNumber` (type int)
- `value` (type int)

**Returns:** type HardwareActionMatcher

### createCCActionMatcher(int,int)

Creates a matcher that recognizes an action when getting a MIDI CC event regardless of the value.

**Parameters:**
- `channel` (type int)
- `controlNumber` (type int)

**Returns:** type HardwareActionMatcher

### createNoteOnActionMatcher(int,int)

Creates a matcher that recognizes an action when a MIDI note on event occurs.

**Parameters:**
- `channel` (type int)
- `note` (type int)

**Returns:** type HardwareActionMatcher

### createNoteOnVelocityValueMatcher(int,int)

Creates a matcher that recognizes a note's on velocity when a MIDI note on event occurs.

**Parameters:**
- `channel` (type int)
- `note` (type int)

**Returns:** type AbsoluteHardwareValueMatcher

### createNoteOffVelocityValueMatcher(int,int)

Creates a matcher that recognizes a note's off velocity when a MIDI note off event occurs.

**Parameters:**
- `channel` (type int)
- `note` (type int)

**Returns:** type AbsoluteHardwareValueMatcher

### createNoteOffActionMatcher(int,int)

Creates a matcher that recognizes an action when a MIDI note off event occurs.

**Parameters:**
- `channel` (type int)
- `note` (type int)

**Returns:** type HardwareActionMatcher

### createActionMatcher(java.lang.String)

Creates a matcher that can match an action from a MIDI event. For example, pressing a button based on
 input of a MIDI CC event.

**Parameters:**
- `expression` (type String): Expression returns true if the event matches

**Returns:** type HardwareActionMatcher

### hardwareAddress()

**Returns:** type String - The address of the hardware device this port belongs to. If two ports belong to the same physical device,
 they have the same address.

