# NoteStep

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Object that describes the content of a step at a given position: x for the time, and y for the key.

## Methods

### x()

**Returns:** type int - the position of the step (time)

### y()

**Returns:** type int - the position of the step (key)

### channel()

**Returns:** type int - the note's channel, in the range 0..15.

### state()

**Returns:** type NoteStep.State - the state of the step, it lets you know if a note starts.

### velocity()

**Returns:** type double - the velocity of the step, in the range 0..1

### setVelocity(double)

If there is a note started at this position, it will update the velocity of the note.

**Parameters:**
- `velocity` (type double): between 0 and 1

**Returns:** type void

### releaseVelocity()

**Returns:** type double - the release velocity of the step, in the range 0..1

### setReleaseVelocity(double)

If there is a note started at this position, it will update the release velocity of the note.

**Parameters:**
- `velocity` (type double): between 0 and 1

**Returns:** type void

### velocitySpread()

**Returns:** type double

### setVelocitySpread(double)

**Parameters:**
- `amount` (type double): velocity spread amount in the range 0..1

**Returns:** type void

### duration()

**Returns:** type double - the duration of the step in beats

### setDuration(double)

If there is a note started at this position, it will update the duration of the note.

**Parameters:**
- `duration` (type double): in beats

**Returns:** type void

### pan()

**Returns:** type double - the pan of the step in the range -1..1

### setPan(double)

If there is a note started at this position, it will update the panning of the note.

**Parameters:**
- `pan` (type double): -1 for left, +1 for right

**Returns:** type void

### timbre()

**Returns:** type double - the timbre of the step, in the range -1..1

### setTimbre(double)

If there is a note started at this position, it will update the timbre of the note.

**Parameters:**
- `timbre` (type double): from -1 to +1

**Returns:** type void

### pressure()

**Returns:** type double - the pressure of the step, in the range 0..1

### setPressure(double)

If there is a note started at this position, it will update the pressure of the note.

**Parameters:**
- `pressure` (type double): from 0 to +1

**Returns:** type void

### gain()

**Returns:** type double - the gain of the step, in the range 0..1

### setGain(double)

If there is a note started at this position, it will update the gain of the note.

**Parameters:**
- `gain` (type double): in the range 0..1, a value of 0.5 results in a gain of 0dB.

**Returns:** type void

### transpose()

**Returns:** type double - the transpose of the step, in semitones

### setTranspose(double)

If there is a note started at this position, it will update the pitch offset of the note.

**Parameters:**
- `transpose` (type double): in semitones, from -96 to +96

**Returns:** type void

### isIsSelected()

**Returns:** type boolean - true if a note exists and is selected

### chance()

Gets the note chance.

**Returns:** type double - the probability, 0..1

### setChance(double)

Sets the note chance.

**Parameters:**
- `chance` (type double): 0..1

**Returns:** type void

### isChanceEnabled()

**Returns:** type boolean

### setIsChanceEnabled(boolean)

**Parameters:**
- `isEnabled` (type boolean)

**Returns:** type void

### isOccurrenceEnabled()

**Returns:** type boolean

### setIsOccurrenceEnabled(boolean)

**Parameters:**
- `isEnabled` (type boolean)

**Returns:** type void

### occurrence()

**Returns:** type NoteOccurrence

### setOccurrence(com.bitwig.extension.controller.api.NoteOccurrence)

**Parameters:**
- `condition` (type NoteOccurrence)

**Returns:** type void

### isRecurrenceEnabled()

**Returns:** type boolean

### setIsRecurrenceEnabled(boolean)

**Parameters:**
- `isEnabled` (type boolean)

**Returns:** type void

### recurrenceLength()

**Returns:** type int

### recurrenceMask()

**Returns:** type int

### setRecurrence(int,int)

**Parameters:**
- `length` (type int): from 1 to 8
- `mask` (type int): bitfield, cycle N -> bit N; max 8 cycles

**Returns:** type void

### isRepeatEnabled()

**Returns:** type boolean

### setIsRepeatEnabled(boolean)

**Parameters:**
- `isEnabled` (type boolean)

**Returns:** type void

### repeatCount()

**Returns:** type int

### setRepeatCount(int)

**Parameters:**
- `count` (type int): -127..127, positive values indicates a number of divisions, negative values a rate.

**Returns:** type void

### repeatCurve()

**Returns:** type double

### setRepeatCurve(double)

**Parameters:**
- `curve` (type double): -1..1

**Returns:** type void

### repeatVelocityEnd()

**Returns:** type double

### setRepeatVelocityEnd(double)

**Parameters:**
- `velocityEnd` (type double): -1..1, relative velocity amount applied to the note on velocity.

**Returns:** type void

### repeatVelocityCurve()

**Returns:** type double

### setRepeatVelocityCurve(double)

**Parameters:**
- `curve` (type double): -1..1

**Returns:** type void

### isMuted()

**Returns:** type boolean - true if the note is muted

### setIsMuted(boolean)

Mutes the note if values is true.

**Parameters:**
- `value` (type boolean)

**Returns:** type void

