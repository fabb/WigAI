# Browser

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent a contextual browser in Bitwig Studio.

**Extends:** `com.bitwig.extension.controller.api.ObjectProxy`

## Methods

### addIsBrowsingObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if a browsing session was started.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receivers a single boolean parameter.

**Returns:** type void

### startBrowsing()

Starts a new browser session.

**Returns:** type void

### cancelBrowsing()

Cancels the current browser session.

**Returns:** type void

### commitSelectedResult()

Finished the browser session by loading the selected item.

**Returns:** type void

### activateSession(com.bitwig.extension.controller.api.BrowsingSession)

Activates the given search session. Please note that only one search session can be active at a time.

**Parameters:**
- `session` (type BrowsingSession): the session that should be activated.

**Returns:** type void

### isWindowMinimized()

Return an object allows to observe and control if the browser window should be small or full-sized.

**Returns:** type SettableBooleanValue - a boolean value object

### shouldAudition()

Return an object allows to observe and control if the selected result should be auditioned.

**Returns:** type SettableBooleanValue - a boolean value object

### createSessionBank(int)

Returns an object that provided bank-wise navigation of the available search sessions. Each search
 session is dedicated to a certain material type, as shown in the tabs of Bitwig Studio's contextual
 browser.

**Parameters:**
- `size` (type int): the size of the windows used to navigate the available browsing sessions.

**Returns:** type BrowsingSessionBank - the requested file column bank object

### createCursorSession()

Returns an object that represents the selected tab as shown in Bitwig Studio's contextual browser
 window.

**Returns:** type CursorBrowsingSession - the requested browsing session cursor

### getDeviceSession()

Returns an object that provides access to the contents of the device tab as shown in Bitwig Studio's
 contextual browser window.

**Returns:** type DeviceBrowsingSession - the requested device browsing session instance

### getPresetSession()

Returns an object that provides access to the contents of the preset tab as shown in Bitwig Studio's
 contextual browser window.

**Returns:** type PresetBrowsingSession - the requested preset browsing session instance

### getSampleSession()

Returns an object that provides access to the contents of the samples tab as shown in Bitwig Studio's
 contextual browser window.

**Returns:** type SampleBrowsingSession - the requested sample browsing session instance

### getMultiSampleSession()

Returns an object that provides access to the contents of the multi-samples tab as shown in Bitwig
 Studio's contextual browser window.

**Returns:** type MultiSampleBrowsingSession - the requested multi-sample browsing session instance

### getClipSession()

Returns an object that provides access to the contents of the clips tab as shown in Bitwig Studio's
 contextual browser window.

**Returns:** type ClipBrowsingSession - the requested clip browsing session instance

### getMusicSession()

Returns an object that provides access to the contents of the music tab as shown in Bitwig Studio's
 contextual browser window.

**Returns:** type MusicBrowsingSession - the requested music browsing session instance

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

