# BrowserResultsColumn

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface are used to navigate a results column in the Bitwig Studio browser.

**Extends:** `com.bitwig.extension.controller.api.BrowserColumn`

## Methods

### createCursorItem()

Returns the cursor result item, which can be used to navigate over the list of entries.

**Returns:** type BrowserResultsItem - the requested filter item object

### createItemBank(int)

Returns an object that provides access to a bank of successive entries using a window configured with
 the given size, that can be scrolled over the list of entries.

**Parameters:**
- `size` (type int): the number of simultaneously accessible items

**Returns:** type BrowserResultsItemBank - the requested item bank object

### addExistsObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the column exists.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter

**Returns:** type void

### entryCount()

Value that reports the underlying total count of column entries (not the size of the column window).

**Returns:** type IntegerValue

### addEntryCountObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the underlying total count of column entries (not the size of the
 column window).

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer parameter

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

