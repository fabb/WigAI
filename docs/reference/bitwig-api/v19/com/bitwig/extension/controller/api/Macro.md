# Macro

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface are used to represent macro controls in Bitwig Studio to controllers.

## Methods

### getAmount()

Returns an object that provides access to the control value of the macro.

**Returns:** type Parameter - a ranged value object.

### getModulationSource()

Returns an object that provides access to the modulation source of the macro.

**Returns:** type ModulationSource - a modulation source object.

### addLabelObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the label of the macro control.

**Parameters:**
- `numChars` (type int): the maximum number of characters of the reported label
- `textWhenUnassigned` (type String): the default text that is reported when the macro is not connected to a Bitwig Studio macro
           control.
- `callback` (type StringValueChangedCallback): a callback function that receives a single string parameter.

**Returns:** type void

