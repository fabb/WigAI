# MasterRecorder

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Controls the project's master recording.

## Methods

### isActive()

Value that indicates if the master recording is active.

**Returns:** type BooleanValue

### start()

Starts the master recording.

**Returns:** type void

### stop()

Stops the master recording.

**Returns:** type void

### toggle()

Toggles the master recording.

**Returns:** type void

### duration()

Get the master recording duration in milliseconds.
 Only relevant when master recording is active.

**Returns:** type IntegerValue

