# HardwareDevice

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents a hardware device that the user has chosen to communicate with. The hardware devices that the
 user needs to choose are defined by the
 ControllerExtensionDefinition.listHardwareDevices(HardwareDeviceMatcherList) method.

## Methods

### deviceMatcher()

The HardwareDeviceMatcher that was provided by the controller for identifying this hardware
 device in ControllerExtensionDefinition.listHardwareDevices(HardwareDeviceMatcherList).

**Returns:** type HardwareDeviceMatcher

