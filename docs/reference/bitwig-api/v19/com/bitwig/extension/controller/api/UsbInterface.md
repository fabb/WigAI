# UsbInterface

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

## Methods

### interfaceMatcher()

The UsbInterfaceMatcher that was provided by the controller for identifying this device.

**Returns:** type UsbInterfaceMatcher

### pipes()

The list of pipes that have been opened for this interface.

**Returns:** type List<UsbPipe>

### pipe(int)

**Parameters:**
- `index` (type int)

**Returns:** type UsbPipe

### pipeCount()

**Returns:** type int

