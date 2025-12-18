# UsbPipe

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Defines a pipe for talking to an endpoint on a USB device.

**Extends:** `com.bitwig.extension.controller.api.Pipe`

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

