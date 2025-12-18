# CursorBrowserFilterColumn

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface are used to navigate the filter columns of a Bitwig Studio browsing session.

**Extends:** `com.bitwig.extension.controller.api.BrowserFilterColumn`, `com.bitwig.extension.controller.api.Cursor`

## Methods

### getWildcardItem()

Returns the filter item that represents the top-level all/any/everything wildcard item.

**Returns:** type BrowserFilterItem - the requested filter item object

### createCursorItem()

Returns the cursor filter item, which can be used to navigate over the list of entries.

**Returns:** type BrowserFilterItem - the requested filter item object

### createItemBank(int)

Returns an object that provides access to a bank of successive entries using a window configured with
 the given size, that can be scrolled over the list of entries.

**Parameters:**
- `size` (type int): the number of simultaneously accessible items

**Returns:** type BrowserFilterItemBank - the requested item bank object

### name()

Value that reports the name of the filter column.

**Returns:** type StringValue

### addNameObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the name of the filter column.

**Parameters:**
- `maxCharacters` (type int)
- `textWhenUnassigned` (type String)
- `callback` (type StringValueChangedCallback): a callback function that receives a single string argument.

**Returns:** type void

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

### selectPrevious()

Select the previous item.

**Returns:** type void

### selectPreviousAction()

**Returns:** type HardwareActionBindable

### selectNext()

Select the next item.

**Returns:** type void

### selectNextAction()

**Returns:** type HardwareActionBindable

### selectFirst()

Select the first item.

**Returns:** type void

### selectLast()

Select the last item.

**Returns:** type void

### hasNext()

Boolean value that reports whether there is an item after the current cursor position.

**Returns:** type BooleanValue

### hasPrevious()

Boolean value that reports whether there is an item before the current cursor position.

**Returns:** type BooleanValue

### addCanSelectPreviousObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers a function with bool argument that gets called when the previous item gains or remains
 selectable.

**Parameters:**
- `callback` (type BooleanValueChangedCallback)

**Returns:** type void

### addCanSelectNextObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers a function with bool argument that gets called when the next item gains or remains
 selectable.

**Parameters:**
- `callback` (type BooleanValueChangedCallback)

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

