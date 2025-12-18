# UsbInputPipe

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

**Extends:** `com.bitwig.extension.controller.api.UsbPipe`, `com.bitwig.extension.controller.api.InputPipe`

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

### readAsync(com.bitwig.extension.api.MemoryBlock,com.bitwig.extension.controller.api.AsyncTransferCompledCallback,int)

Requests to read some data from this pipe in an asynchronous way (the caller is not blocked). Once some
 data has been read the callback will be notified on the controller's thread.

**Parameters:**
- `data` (type MemoryBlock): A
- `callback` (type AsyncTransferCompledCallback): A callback that is notified on the controller's thread when the read has completed.
- `timeoutInMs` (type int): A timeout in milliseconds that will result in an error and termination of the controller if
           the read does not happen in this time. For inifnite timeout use 0.

**Returns:** type void

### read(com.bitwig.extension.api.MemoryBlock,int)

Requests to read some data from this pipe in a synchronous way (the caller is blocked until the transfer
 completes).

**Parameters:**
- `data` (type MemoryBlock)
- `timeoutInMs` (type int): A timeout in milliseconds that will result in an error and termination of the controller if
           the read does not happen in this time. For inifinite timeout use 0.

**Returns:** type int - The number of bytes that was read.

