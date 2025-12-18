# UsbOutputPipe

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

**Extends:** `com.bitwig.extension.controller.api.UsbPipe`, `com.bitwig.extension.controller.api.OutputPipe`

## Methods

### device()

The device this endpoint is on.

**Returns:** type UsbDevice

### endpointMatcher()

The UsbEndpointMatcher that was provided by the controller for identifying the endpoint to use
 for communication.

**Returns:** type UsbEndpointMatcher

### endpointAddress()

The endpoint address on the device that this endpoint is for.

**Returns:** type byte

### direction()

UsbTransferDirection for this pipe.

**Returns:** type UsbTransferDirection

### transferType()

The UsbTransferType type that this pipe uses for communicating with the USB device.

**Returns:** type UsbTransferType

### writeAsync(com.bitwig.extension.api.MemoryBlock,com.bitwig.extension.controller.api.AsyncTransferCompledCallback,int)

Requests to write some data to this pipe in an asynchronous way (the caller is not blocked). Once some
 data has been written the callback will be notified on the controller's thread.

**Parameters:**
- `data` (type MemoryBlock): A
- `callback` (type AsyncTransferCompledCallback): A callback that is notified on the controller's thread when the write has completed.
- `timeoutInMs` (type int): A timeout in milliseconds that will result in an error and termination of the controller if
           the write does not happen in this time. For infinite timeout use 0.

**Returns:** type void

### write(com.bitwig.extension.api.MemoryBlock,int)

**Parameters:**
- `data` (type MemoryBlock)
- `timeoutInMs` (type int)

**Returns:** type int

