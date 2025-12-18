# BeatTimeFormatter

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Defines a formatter for a beat time that can convert a beat time to a string for display to the user.

## Methods

### formatBeatTime(double,boolean,int,int,int)

Formats the supplied beat time as a string in the supplied time signature.

**Parameters:**
- `beatTime` (type double): The beat time to be formatted
- `isAbsolute` (type boolean): If true the beat time represents an absolute time (such as a time on the arranger) otherwise
           it represents a beat time duration (such as the length of a clip).
- `timeSignatureNumerator` (type int)
- `timeSignatureDenominator` (type int)
- `timeSignatureTicks` (type int)

**Returns:** type String

