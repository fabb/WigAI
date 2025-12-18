# MidiExpressions

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Creates useful MIDI expressions that can be used to match MIDI events.

## Methods

### createIsCCExpression(int)

Creates an expression that recognizes a MIDI CC event regardless of its channel.

**Parameters:**
- `controlNumber` (type int)

**Returns:** type String

### createIsCCExpression(int,int)

Creates an expression that recognizes a MIDI CC event.

**Parameters:**
- `channel` (type int)
- `controlNumber` (type int)

**Returns:** type String

### createIsCCValueExpression(int,int,int)

Creates an expression that recognizes a MIDI CC event with a specific value. This expression can be used
 in 

invalid reference
#createActionMatcher(String)

 or 

invalid reference
#createAbsoluteValueMatcher(String, String, int)

, for
 example.

**Parameters:**
- `channel` (type int)
- `control` (type int)
- `value` (type int)

**Returns:** type String

### createIsPitchBendExpression(int)

Creates an expression that recognizes a pitch bend event. This expression can be used in
 

invalid reference
#createActionMatcher(String)

 or 

invalid reference
#createAbsoluteValueMatcher(String, String, int)

, for
 example.

**Parameters:**
- `channel` (type int)

**Returns:** type String

### createIsNoteOnExpression(int,int)

Creates an expression that recognizes a note on event. This expression can be used in
 

invalid reference
#createActionMatcher(String)

 or 

invalid reference
#createAbsoluteValueMatcher(String, String, int)

, for
 example.

**Parameters:**
- `channel` (type int)
- `note` (type int)

**Returns:** type String

### createIsNoteOffExpression(int,int)

Creates an expression that recognizes a note off event. This expression can be used in
 

invalid reference
#createActionMatcher(String)

 or 

invalid reference
#createAbsoluteValueMatcher(String, String, int)

, for
 example.

**Parameters:**
- `channel` (type int)
- `note` (type int)

**Returns:** type String

### createIsPolyAftertouch(int,int)

Creates an expression that recognizes a polyphonic aftertouch event. This expression can be used in
 

invalid reference
#createActionMatcher(String)

 or 

invalid reference
#createAbsoluteValueMatcher(String, String, int)

, for
 example.

**Parameters:**
- `channel` (type int)
- `note` (type int)

**Returns:** type String

