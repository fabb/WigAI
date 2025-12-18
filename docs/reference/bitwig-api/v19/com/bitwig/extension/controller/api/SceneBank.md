# SceneBank

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

A scene bank provides access to a range of scenes in Bitwig Studio. Instances of scene bank are configured
 with a fixed number of scenes and represent an excerpt of a larger list of scenes. Various methods are
 provided for scrolling to different sections of the scene list. It basically acts like a window moving over
 the list of underlying scenes.

 To receive an instance of scene bank call
 ControllerHost.createSceneBank(int).

**Extends:** `com.bitwig.extension.controller.api.ClipLauncherSlotOrSceneBank`

## Methods

### getScene(int)

Returns the scene at the given index within the bank.

**Parameters:**
- `indexInBank` (type int): the scene index within this bank, not the index within the list of all Bitwig Studio scenes.
           Must be in the range [0..sizeOfBank-1].

**Returns:** type Scene - the requested scene object

### scrollPageUp()

Scrolls the scenes one page up.

**Returns:** type void

### scrollPageDown()

Scrolls the scenes one page down.

**Returns:** type void

### scrollUp()

Scrolls the scenes one scene up.

**Returns:** type void

### scrollDown()

Scrolls the scenes one scene down.

**Returns:** type void

### scrollTo(int)

Makes the scene with the given position visible in the track bank.

**Parameters:**
- `position` (type int): the position of the scene within the underlying full list of scenes

**Returns:** type void

### addScrollPositionObserver(com.bitwig.extension.callback.IntegerValueChangedCallback,int)

Registers an observer that reports the current scene scroll position.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that takes a single integer parameter
- `valueWhenUnassigned` (type int): the default value that gets reports when the track bank is not yet connected to a Bitwig
           Studio document

**Returns:** type void

### addCanScrollUpObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the scene window can be scrolled further up.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter

**Returns:** type void

### addCanScrollDownObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the scene window can be scrolled further down.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter

**Returns:** type void

### addSceneCountObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the underlying total scene count (not the number of scenes available
 in the bank window).

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer parameter

**Returns:** type void

### launchScene(int)

Launches the scene with the given bank index.

**Parameters:**
- `indexInWindow` (type int): the scene index within the bank, not the position of the scene withing the underlying full
           list of scenes.

**Returns:** type void

### setIndication(boolean)

Specifies if the Bitwig Studio clip launcher should indicate which scenes are part of the window. By
 default indications are disabled.

**Parameters:**
- `shouldIndicate` (type boolean): `true` if visual indications should be enabled, `false` otherwise

**Returns:** type void

### launch(int)

Launches the scene/slot with the given index.

**Parameters:**
- `slot` (type int): the index of the slot that should be launched

**Returns:** type void

### launchAlt(int)

Launches the scene/slot with the given index.

**Parameters:**
- `slot` (type int): the index of the slot that should be launched

**Returns:** type void

### stop()

Stops clip launcher playback for the associated track.

**Returns:** type void

### stopAlt()

Stops clip launcher playback for the associated track.

**Returns:** type void

### stopAction()

Action to call stop().

**Returns:** type HardwareActionBindable

### stopAltAction()

Action to call stopAlt().

**Returns:** type HardwareActionBindable

### returnToArrangement()

Performs a return-to-arrangement operation on the related track, which caused playback to be taken over
 by the arrangement sequencer.

**Returns:** type void

### addNameObserver(com.bitwig.extension.callback.IndexedStringValueChangedCallback)

Registers an observer that reports the names of the scenes and slots. The slot names reflect the names
 of containing clips.

**Parameters:**
- `callback` (type IndexedStringValueChangedCallback): a callback function receiving two parameters: 1. the slot index (integer) within the
           configured window, and 2. the name of the scene/slot (string)

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

