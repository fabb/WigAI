# DeviceBank

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

This interface is used for navigation of device chains in Bitwig Studio. Instances are configured with a
 fixed number of devices and provide access to a excerpt of the devices inside a device chain. Various
 methods are provided for scrolling to different sections of the device chain. It basically acts like a
 window moving over the devices.

 To receive an instance of DeviceBank call DeviceChain.createDeviceBank(int).

**Extends:** `com.bitwig.extension.controller.api.Bank`

## Methods

### getDeviceChain()

Returns the object that was used to instantiate this device bank. Possible device chain instances are
 tracks, device layers, drums pads, or FX slots.

**Returns:** type DeviceChain - the requested device chain object

### getDevice(int)

Returns the device at the given index within the bank.

**Parameters:**
- `indexInBank` (type int): the device index within this bank, not the position within the device chain. Must be in the
           range [0..sizeOfBank-1].

**Returns:** type Device - the requested device object

### scrollPageUp()

Scrolls the device window one page up.

**Returns:** type void

### scrollPageDown()

Scrolls the device window one page down.

**Returns:** type void

### scrollUp()

Scrolls the device window one device up.

**Returns:** type void

### scrollDown()

Scrolls the device window one device down.

**Returns:** type void

### scrollTo(int)

Makes the device with the given position visible in the track bank.

**Parameters:**
- `position` (type int): the position of the device within the device chain

**Returns:** type void

### addScrollPositionObserver(com.bitwig.extension.callback.IntegerValueChangedCallback,int)

Registers an observer that reports the current device scroll position.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that takes a single integer parameter
- `valueWhenUnassigned` (type int): the default value that gets reports when the device chain is not yet connected to a Bitwig
           Studio document

**Returns:** type void

### addCanScrollUpObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device window can be scrolled further up.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter

**Returns:** type void

### addCanScrollDownObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device window can be scrolled further down.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter

**Returns:** type void

### addDeviceCountObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the total device count of the device chain (not the number of devices
 accessible through the bank window).

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer parameter

**Returns:** type void

### browseToInsertDevice(int)

Browses for content to insert a device at the given index inside this bank.

**Parameters:**
- `index` (type int): the index to insert the device at. Must be >= 0 and <=

**Returns:** type void

### setDeviceMatcher(com.bitwig.extension.controller.api.DeviceMatcher)

Sets a DeviceMatcher that can be used to filter devices in this bank to show only those
 matching the supplied matcher.

**Parameters:**
- `matcher` (type DeviceMatcher): The matcher that should filter the devices or null if all devices should be matched

**Returns:** type void

### getSizeOfBank()

The fixed size of this bank.
 This will be initially equal to the capacity of the Bank.

**Returns:** type int

### getCapacityOfBank()

The maximum number of items in the bank which is defined when the bank is initially created.

**Returns:** type int

### setSizeOfBank(int)

Sets the size of this bank

**Parameters:**
- `size` (type int): number of items in the bank that has to be greater than 0 and less or equal to the capacity of the bank.

**Returns:** type void

### getItemAt(int)

Gets the item in the bank at the supplied index. The index must be >= 0 and < getSizeOfBank().

**Parameters:**
- `index` (type int)

**Returns:** type ItemType

### itemCount()

Value that reports the underlying total item count (not the number of items available in the bank
 window).

**Returns:** type IntegerValue

### cursorIndex()

An integer value that defines the location of the cursor that this bank is following. If there is no
 cursor or the cursor is not within the bank then the value is -1.

**Returns:** type SettableIntegerValue

### setSkipDisabledItems(boolean)

Disabled items will not be accessible via the bank if set to true.

**Parameters:**
- `shouldSkip` (type boolean)

**Returns:** type void

### exists()

Returns a value object that indicates if the object being proxied exists, or if it has content.

**Returns:** type BooleanValue

### createEqualsValue(com.bitwig.extension.controller.api.ObjectProxy)

Creates a BooleanValue that determines this proxy is considered equal to another proxy. For this
 to be the case both proxies need to be proxying the same target object.

**Parameters:**
- `other` (type ObjectProxy)

**Returns:** type BooleanValue

### isSubscribed()

Determines if this object is currently 'subscribed'. In the subscribed state it will notify any
 observers registered on it.

**Returns:** type boolean

### setIsSubscribed(boolean)

Sets whether the driver currently considers this object 'active' or not.

**Parameters:**
- `value` (type boolean)

**Returns:** type void

### subscribe()

Subscribes the driver to this object.

**Returns:** type void

### unsubscribe()

Unsubscribes the driver from this object.

**Returns:** type void

### scrollPosition()

Value that reports the current scroll position.

**Returns:** type SettableIntegerValue

### scrollIntoView(int)

Scrolls the supplied position into view if it isn't already.

**Parameters:**
- `position` (type int)

**Returns:** type void

### scrollBy(int)

Scrolls by a number of steps.

**Parameters:**
- `amount` (type int): The number of steps to scroll by (positive is forwards and negative is backwards).

**Returns:** type void

### scrollForwards()

Scrolls forwards by one step. This is the same as calling scrollBy(int) with 1

**Returns:** type void

### scrollForwardsAction()

**Returns:** type HardwareActionBindable

### scrollBackwards()

Scrolls forwards by one step. This is the same as calling scrollBy(int) with -1

**Returns:** type void

### scrollBackwardsAction()

**Returns:** type HardwareActionBindable

### scrollByPages(int)

Scrolls by a number of pages.

**Parameters:**
- `amount` (type int): The number of pages to scroll by (positive is forwards and negative is backwards).

**Returns:** type void

### scrollPageForwards()

Scrolls forwards by one page.

**Returns:** type void

### scrollPageForwardsAction()

**Returns:** type HardwareActionBindable

### scrollPageBackwards()

Scrolls backwards by one page.

**Returns:** type void

### scrollPageBackwardsAction()

**Returns:** type HardwareActionBindable

### canScrollBackwards()

Value that reports if it is possible to scroll the bank backwards or not.

**Returns:** type BooleanValue

### canScrollForwards()

Value that reports if it is possible to scroll the bank forwards or not.

**Returns:** type BooleanValue

### addBinding(com.bitwig.extension.controller.api.RelativeHardwareControl)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)

**Returns:** type RelativeHardwareControlBinding

### addBindingWithSensitivity(com.bitwig.extension.controller.api.RelativeHardwareControl,double)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)
- `sensitivity` (type double)

**Returns:** type RelativeHardwareControlBinding

