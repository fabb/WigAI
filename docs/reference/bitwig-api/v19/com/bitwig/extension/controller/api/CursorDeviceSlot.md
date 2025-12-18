# CursorDeviceSlot

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent the selected device slot as shown in the Bitwig Studio user
 interface.

**Extends:** `com.bitwig.extension.controller.api.DeviceChain`

## Methods

### selectSlot(java.lang.String)

**Parameters:**
- `slot` (type String)

**Returns:** type void

### selectInEditor()

Selects the device chain in Bitwig Studio, in case it is a selectable object.

**Returns:** type void

### name()

Value that reports the name of the device chain, such as the track name or the drum pad
 name.

**Returns:** type SettableStringValue

### addNameObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the name of the device chain, such as the track name or the drum pad
 name.

**Parameters:**
- `numChars` (type int): the maximum number of characters used for the reported name
- `textWhenUnassigned` (type String): the default text that gets reported when the device chain is not associated with an object in
           Bitwig Studio yet.
- `callback` (type StringValueChangedCallback): a callback function that receives a single name parameter (string).

**Returns:** type void

### addIsSelectedInEditorObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device chain is selected in Bitwig Studio editors.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter.

**Returns:** type void

### addIsSelectedObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

**Parameters:**
- `callback` (type BooleanValueChangedCallback)

**Returns:** type void

### createDeviceBank(int)

Returns an object that provides bank-wise navigation of devices.

**Parameters:**
- `numDevices` (type int): the number of devices should be accessible simultaneously

**Returns:** type DeviceBank - the requested device bank object

### createDeviceBrowser(int,int)

Returns an object used for browsing devices, presets and other content. Committing the browsing session
 will load or create a device from the selected resource and insert it into the device chain.

**Parameters:**
- `numFilterColumnEntries` (type int): the size of the window used to navigate the filter column entries.
- `numResultsColumnEntries` (type int): the size of the window used to navigate the results column entries.

**Returns:** type Browser - the requested device browser object.

### select()

**Returns:** type void

### browseToInsertAtStartOfChain()

Starts browsing for content that can be inserted at the start of this device chain.

**Returns:** type void

### browseToInsertAtEndOfChain()

Starts browsing for content that can be inserted at the end of this device chain.

**Returns:** type void

### startOfDeviceChainInsertionPoint()

InsertionPoint that can be used to insert at the start of the device chain.

**Returns:** type InsertionPoint

### endOfDeviceChainInsertionPoint()

InsertionPoint that can be used to insert at the end of the device chain.

**Returns:** type InsertionPoint

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

