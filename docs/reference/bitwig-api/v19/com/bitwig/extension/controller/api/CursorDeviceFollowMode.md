# CursorDeviceFollowMode

- Kind: enum
- Package: `com.bitwig.extension.controller.api`

Mode that defines how a CursorDevice follows a device within the CursorTrack it is created
 for by default. The user can still override this on a track by track basis but this defines a default
 follow mode when the user has not done this.

#### Values

- `FOLLOW_SELECTION`: Follows the device selection made by the user in the track.
- `FIRST_DEVICE`: Selects the first device in the track if there is one.
- `FIRST_INSTRUMENT`: Selects the first instrument in the track if there is one.
- `FIRST_AUDIO_EFFECT`: Selects the first audio effect in the track if there is one.
- `FIRST_INSTRUMENT_OR_DEVICE`: Selects the first instrument or if there is no instrument the first device.
- `LAST_DEVICE`: Selects the last device in the track if there is one.

## Methods

### values()

Returns an array containing the constants of this enum class, in
the order they are declared.

**Returns:** type CursorDeviceFollowMode[] - an array containing the constants of this enum class, in the order they are declared

### valueOf(java.lang.String)

Returns the enum constant of this class with the specified name.
The string must match exactly an identifier used to declare an
enum constant in this class.  (Extraneous whitespace characters are 
not permitted.)

**Parameters:**
- `name` (type String): the name of the enum constant to be returned.

**Returns:** type CursorDeviceFollowMode - the enum constant with the specified name

