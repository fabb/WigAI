# UsbDevice

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Defines a USB device that is available for communication.

**Extends:** `com.bitwig.extension.controller.api.HardwareDevice`

## Methods

### deviceMatcher()

The UsbDeviceMatcher that was provided by the controller for identifying this device.

**Returns:** type UsbDeviceMatcher

### ifaces()

The list of UsbInterfaces that have been opened for this device.

**Returns:** type List<UsbInterface>

### iface(int)

The UsbInterface that was claimed using the UsbInterfaceMatcher defined at the
 corresponding index in the UsbDeviceMatcher.

**Parameters:**
- `index` (type int)

**Returns:** type UsbInterface

