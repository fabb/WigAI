# DirectParameterValueDisplayObserver

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

This interface is used to configure observation of pretty-printed device parameter values.

## Methods

### setObservedParameterIds(java.lang.String[])

Starts observing the parameters according to the given parameter ID array, or stops observing in case
 `null` is passed in for the parameter ID array.

**Parameters:**
- `parameterIds` (type String[]): the array of parameter IDs or `null` to stop observing parameter display values.

**Returns:** type void

