# ModulationSource

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

This interface represents a modulation source in Bitwig Studio.

## Methods

### isMapping()

Value which reports when the modulation source is in mapping mode.

**Returns:** type BooleanValue

### addIsMappingObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer which reports when the modulation source is in mapping mode.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter

**Returns:** type void

### toggleIsMapping()

Toggles the modulation source between mapping mode and normal control functionality.

**Returns:** type void

### name()

Value the reports the name of the modulation source.

**Returns:** type StringValue

### addNameObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer the reports the name of the modulation source.

**Parameters:**
- `numChars` (type int): the maximum number of character the reported name should be long
- `textWhenUnassigned` (type String): the default text that gets reported if the modulation source is not connected to to a
           modulation source in Bitwig Studio yet
- `callback` (type StringValueChangedCallback): a callback function that receives a single string parameter

**Returns:** type void

### isMapped()

Value which reports if the modulation source is mapped to any destination(s).

**Returns:** type BooleanValue

### addIsMappedObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer which reports if the modulation source is mapped to any destination(s).

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter

**Returns:** type void

