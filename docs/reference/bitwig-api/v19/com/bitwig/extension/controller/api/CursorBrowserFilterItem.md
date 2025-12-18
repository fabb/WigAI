# CursorBrowserFilterItem

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent entries in a browser filter column.

**Extends:** `com.bitwig.extension.controller.api.BrowserFilterItem`, `com.bitwig.extension.controller.api.CursorBrowserItem`

## Methods

### selectParent()

Select the parent item.

**Returns:** type void

### selectFirstChild()

Select the first child item.

**Returns:** type void

### selectLastChild()

Select the last child item.

**Returns:** type void

### moveToPrevious()

Select the previous item.

**Returns:** type void

### moveToNext()

Select the next item.

**Returns:** type void

### moveToFirst()

Select the first item.

**Returns:** type void

### moveToLast()

Select the last item.

**Returns:** type void

### moveToParent()

Select the parent item.

**Returns:** type void

### moveToFirstChild()

Move the cursor to the first child item.

**Returns:** type void

### moveToLastChild()

Move the cursor to the last child item.

**Returns:** type void

### hitCount()

Value that reports the hit count of the filter item.

**Returns:** type IntegerValue

### addHitCountObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the hit count of the filter item.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer parameter

**Returns:** type void

### addExistsObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the item exists.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter

**Returns:** type void

### name()

Value that reports the name of the browser item.

**Returns:** type StringValue

### addValueObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the string value of the browser item.

**Parameters:**
- `maxCharacters` (type int)
- `textWhenUnassigned` (type String)
- `callback` (type StringValueChangedCallback): a callback function that receives a single string argument

**Returns:** type void

### isSelected()

Returns an object that provides access to the selected state of the browser item.

**Returns:** type SettableBooleanValue - an boolean value object

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

### createSiblingsBank(int)

Returns a bank object that provides access to the siblings of the cursor item. The bank will
 automatically scroll so that the cursor item is always visible.

**Parameters:**
- `numSiblings` (type int): the number of simultaneously accessible siblings

**Returns:** type BrowserItemBank - the requested item bank object

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

