# HardwareDeviceMatcherList

- Kind: class
- Package: `com.bitwig.extension.controller`

Defines a list of all the hardware devices that a controller needs.

**Extends:** java.lang.Object

## Methods

### add(com.bitwig.extension.controller.HardwareDeviceMatcher...)

Adds information about a hardware device that is needed and how it can be matched. The hardware device
 will need to match at least one of the supplied matchers.

 For each entry added to this list the user will see a device chooser that lets them select an
 appropriate device. The information added here is also used for auto detection purposes.

**Parameters:**
- `deviceMatchers` (type HardwareDeviceMatcher...)

**Returns:** type void

### getCount()

The number of hardware devices in the list.

**Returns:** type int

### getHardwareDeviceMatchersAt(int)

**Parameters:**
- `index` (type int)

**Returns:** type HardwareDeviceMatcher[]

### getList()

**Returns:** type List<HardwareDeviceMatcher[]>

