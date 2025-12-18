# ParameterBank

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Defines a bank of parameters.

## Methods

### getParameterCount()

Gets the number of slots that these remote controls have.

**Returns:** type int

### getParameter(int)

Returns the parameter at the given index within the bank.

**Parameters:**
- `indexInBank` (type int): the parameter index within this bank. Must be in the range [0..getParameterCount()-1].

**Returns:** type Parameter - the requested parameter

### setHardwareLayout(com.bitwig.extension.controller.api.HardwareControlType,int)

Informs the application how to display the controls during the on screen notification.

**Parameters:**
- `type` (type HardwareControlType): which kind of hardware control is used for this bank (knobs/encoders/sliders)
- `columns` (type int): How wide this section is in terms of layout (4/8/9)

**Returns:** type void

