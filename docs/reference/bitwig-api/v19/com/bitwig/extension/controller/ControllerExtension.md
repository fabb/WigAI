# ControllerExtension

- Kind: abstract class
- Package: `com.bitwig.extension.controller`

Defines an extension that enabled a controller to work with Bitwig Studio.

**Extends:** com.bitwig.extension.Extension

## Methods

### getMidiInPort(int)

**Parameters:**
- `index` (type int)

**Returns:** type MidiIn

### getMidiOutPort(int)

**Parameters:**
- `index` (type int)

**Returns:** type MidiOut

### init()

Initializes this controller extension. This will be called once when the extension is started. During initialization the
 extension should call the various create methods available via the ControllerHost interface in order to
 create objects used to communicate with various parts of the Bitwig Studio application (e.g
 ControllerHost.createCursorTrack(int, int).

**Returns:** type void

### exit()

Called once when this controller extension is stopped.

**Returns:** type void

### flush()

Called when this controller extension should flush any pending updates to the controller.

**Returns:** type void

### getHost()

**Returns:** type HostType

### getExtensionDefinition()

**Returns:** type DefinitionType

