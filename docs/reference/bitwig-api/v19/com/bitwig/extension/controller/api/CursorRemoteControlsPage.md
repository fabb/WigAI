# CursorRemoteControlsPage

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents a cursor that looks at a RemoteControlsPage.

**Extends:** `com.bitwig.extension.controller.api.Cursor`, `com.bitwig.extension.controller.api.RemoteControlsPage`

## Methods

### pageNames()

Value that reports the names of the devices parameter pages.

**Returns:** type StringArrayValue

### selectNextPage(boolean)

Selects the next page.

**Parameters:**
- `shouldCycle` (type boolean): If true then when the end is reached and there is no next page it selects the first page

**Returns:** type void

### selectPreviousPage(boolean)

Selects the previous page.

**Parameters:**
- `shouldCycle` (type boolean): If true then when the end is reached and there is no next page it selects the first page

**Returns:** type void

### selectNextPageMatching(java.lang.String,boolean)

Selects the next page that matches the given expression.

**Parameters:**
- `expression` (type String): An expression that can match a page based on how it has been tagged. For now this can only be
           the name of a single tag that you would like to match.
- `shouldCycle` (type boolean): If true then when the end is reached and there is no next page it selects the first page

**Returns:** type void

### selectPreviousPageMatching(java.lang.String,boolean)

Selects the previous page that matches the given expression.

**Parameters:**
- `expression` (type String): An expression that can match a page based on how it has been tagged. For now this can only be
           the name of a single tag that you would like to match.
- `shouldCycle` (type boolean): If true then when the end is reached and there is no next page it selects the first page

**Returns:** type void

### selectedPageIndex()

Value that reports the currently selected parameter page index.

**Returns:** type SettableIntegerValue

### pageCount()

Value that represents the number of pages.

**Returns:** type IntegerValue

### createPresetPage()

Creates a new preset page.

**Returns:** type void

### createPresetPageAction()

**Returns:** type HardwareActionBindable

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

### getParameter(int)

Returns the parameter at the given index within the bank.

**Parameters:**
- `indexInBank` (type int): the parameter index within this bank. Must be in the range [0..getParameterCount()-1].

**Returns:** type RemoteControl - the requested parameter

### getName()

**Returns:** type StringValue

### getParameterCount()

Gets the number of slots that these remote controls have.

**Returns:** type int

### setHardwareLayout(com.bitwig.extension.controller.api.HardwareControlType,int)

Informs the application how to display the controls during the on screen notification.

**Parameters:**
- `type` (type HardwareControlType): which kind of hardware control is used for this bank (knobs/encoders/sliders)
- `columns` (type int): How wide this section is in terms of layout (4/8/9)

**Returns:** type void

### deleteObject()

Deletes this object from the document.

 If you want to delete multiple objects at once, see Host.deleteObjects().

**Returns:** type void

### deleteObjectAction()

Deletes this object from the document.

**Returns:** type HardwareActionBindable

