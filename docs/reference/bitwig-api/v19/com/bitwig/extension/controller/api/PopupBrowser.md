# PopupBrowser

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Object that represents the popup browser in Bitwig Studio.

**Extends:** `com.bitwig.extension.controller.api.ObjectProxy`, `com.bitwig.extension.controller.api.RelativeHardwarControlBindable`

## Methods

### title()

The title of the popup browser.

**Returns:** type StringValue

### contentTypeNames()

Value that reports the possible content types that can be inserted by the popup browser. These are
 represented by the tabs in Bitwig Studio's popup browser.

 (e.g "Device", "Preset", "Sample" etc.)

**Returns:** type StringArrayValue

### selectedContentTypeName()

Value that represents the selected content type.

**Returns:** type StringValue

### selectedContentTypeIndex()

Value that represents the index of the selected content type within the content types supported.

**Returns:** type SettableIntegerValue

### smartCollectionColumn()

The smart collections column of the browser.

**Returns:** type BrowserFilterColumn

### locationColumn()

The location column of the browser.

**Returns:** type BrowserFilterColumn

### deviceColumn()

The device column of the browser.

**Returns:** type BrowserFilterColumn

### categoryColumn()

The category column of the browser.

**Returns:** type BrowserFilterColumn

### tagColumn()

The tag column of the browser.

**Returns:** type BrowserFilterColumn

### deviceTypeColumn()

The device type column of the browser.

**Returns:** type BrowserFilterColumn

### fileTypeColumn()

The file type column of the browser.

**Returns:** type BrowserFilterColumn

### creatorColumn()

The creator column of the browser.

**Returns:** type BrowserFilterColumn

### resultsColumn()

Column that represents the results of the search.

**Returns:** type BrowserResultsColumn

### canAudition()

Value that indicates if the browser is able to audition material in place while browsing.

**Returns:** type BooleanValue

### shouldAudition()

Value that decides if the browser is currently auditioning material in place while browsing or not.

**Returns:** type SettableBooleanValue

### selectNextFile()

Selects the next file.

**Returns:** type void

### selectNextFileAction()

Action that selects the next file

**Returns:** type HardwareActionBindable

### selectPreviousFile()

Selects the previous file.

**Returns:** type void

### selectPreviousFileAction()

Action that selects the next file

**Returns:** type HardwareActionBindable

### selectFirstFile()

Selects the first file.

**Returns:** type void

### selectLastFile()

Selects the last file.

**Returns:** type void

### cancel()

Cancels the popup browser.

**Returns:** type void

### cancelAction()

**Returns:** type HardwareActionBindable

### commit()

Commits the selected item in the popup browser.

**Returns:** type void

### commitAction()

**Returns:** type HardwareActionBindable

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

