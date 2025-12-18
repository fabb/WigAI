# GenericBrowsingSession

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface are used for browsing material with bank-wise access to the filter columns.

**Extends:** `com.bitwig.extension.controller.api.BrowsingSession`

## Methods

### name()

Value that reports the name of the browsing session.

**Returns:** type StringValue

### addNameObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the name of the browsing session.

**Parameters:**
- `maxCharacters` (type int)
- `textWhenUnassigned` (type String)
- `callback` (type StringValueChangedCallback): a callback function that receives a single string argument.

**Returns:** type void

### addIsAvailableObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the browser session is available for the current context.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument.

**Returns:** type void

### addIsActiveObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the browser session is currently active.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean argument.

**Returns:** type void

### activate()

Activates the given search session, same as calling Browser#activateSession(this). Please note that only one search session can be active at a time.

**Returns:** type void

### getResults()

Returns an object that represents the column which shows the results according to the current filter
 settings in Bitwig Studio's contextual browser.

**Returns:** type BrowserResultsColumn - the requested results browser column.

### getCursorResult()

Returns an object used for navigating the entries in the results column of Bitwig Studio's contextual
 browser.

**Returns:** type CursorBrowserResultItem - the requested cursor object.

### getSettledResult()

Returns an object that represents the currently loaded material item.

**Returns:** type BrowserResultsItem - the requested settled result object

### getCursorFilter()

Returns an object that can be used to navigate over the various filter sections of the browsing session.

**Returns:** type CursorBrowserFilterColumn - the requested filter cursor object

### createFilterBank(int)

Returns an object that provided bank-wise navigation of filter columns.

**Parameters:**
- `numColumns` (type int): the number of columns that are simultaneously accessible.

**Returns:** type BrowserFilterColumnBank - the requested file column bank object

### hitCount()

Value that reports the number of results available for the current filter settings.

**Returns:** type IntegerValue

### addHitCountObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the number of results available for the current filter settings.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer argument.

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

