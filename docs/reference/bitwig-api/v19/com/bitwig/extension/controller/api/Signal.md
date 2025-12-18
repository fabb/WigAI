# Signal

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

A generic interface used to implement actions or events that are not associated with a value.

## Methods

### addSignalObserver(com.bitwig.extension.callback.NoArgsCallback)

Registers an observer that gets notified when the signal gets fired.

**Parameters:**
- `callback` (type NoArgsCallback): a callback function that does not receive any argument.

**Returns:** type void

### fire()

Fires the action or event represented by the signal object.

**Returns:** type void

