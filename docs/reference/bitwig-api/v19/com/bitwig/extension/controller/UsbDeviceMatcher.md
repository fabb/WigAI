# UsbDeviceMatcher

- Kind: class
- Package: `com.bitwig.extension.controller`

Defines information needed to identify suitable USB devices for use by an extension.

**Extends:** com.bitwig.extension.controller.HardwareDeviceMatcher

## Methods

### getExpression()

An expression that can be used on the USB device descriptor to decide if the device matches.
           Variables in the expression can refer to the following fields of the device descriptor:

           - bDeviceClass - bDeviceSubClass - bDeviceProtocol - idVendor - idProduct

           For example to match a device that has vendor id 0x10 product id 0x20 the expression would be:

           "idVendor == 0x10 invalid input: '&'invalid input: '&' idProduct == 0x20"

**Returns:** type String

### getConfigurationMatcher()

Object that tries to match a configuration on the device that it can use.

**Returns:** type UsbConfigurationMatcher

### getName()

Human friendly name for the kinds of hardware devices this matcher matches.

**Returns:** type String

