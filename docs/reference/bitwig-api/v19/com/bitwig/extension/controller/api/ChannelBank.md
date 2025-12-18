# ChannelBank

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

A channel bank provides access to a range of channels in Bitwig Studio, such as tracks or device layers.
 Instances of channel bank are typically configured with support for a fixed number of channels and
 represent an excerpt of a larger list of channels. Various methods are provided for scrolling to different
 sections of the channel list. It basically acts like a window moving over the list of channels.

**Extends:** `com.bitwig.extension.controller.api.ObjectProxy`, `com.bitwig.extension.controller.api.Bank`

## Methods

### getChannel(int)

Returns the channel for the given index.

**Parameters:**
- `indexInBank` (type int): the channel index within this bank, not the index within the list of all Bitwig Studio
           channels. Must be in the range [0..sizeOfBank-1].

**Returns:** type Channel - the channel object

### setChannelScrollStepSize(int)

Sets the step size used for scrolling the channel bank.

**Parameters:**
- `stepSize` (type int): the step size used for scrolling. Default is `1`.

**Returns:** type void

### scrollChannelsPageUp()

Scrolls the channels one page up. For example if the channel bank is configured with a window size of 8
 channels and is currently showing channel [1..8], calling this method would scroll the channel bank to
 show channel [9..16].

**Returns:** type void

### scrollChannelsPageDown()

Scrolls the channels one page up. For example if the channel bank is configured with a window size of 8
 channels and is currently showing channel [9..16], calling this method would scroll the channel bank to
 show channel [1..8].

**Returns:** type void

### scrollChannelsUp()

Scrolls the channel window up by the amount specified via setChannelScrollStepSize(int) (by
 default one channel).

**Returns:** type void

### scrollChannelsDown()

Scrolls the channel window down by the amount specified via setChannelScrollStepSize(int) (by
 default one channel).

**Returns:** type void

### scrollToChannel(int)

Scrolls the channel bank window so that the channel at the given position becomes visible.

**Parameters:**
- `position` (type int): the index of the channel within the underlying full list of channels (not the index within the
           bank). The position is typically directly related to the layout of the channel list in Bitwig
           Studio, starting with zero in case of the first channel.

**Returns:** type void

### channelScrollPosition()

Value that reports the current scroll position, more specifically the position of the
 first channel within the underlying list of channels, that is shown as channel zero within the bank.

**Returns:** type IntegerValue

### addChannelScrollPositionObserver(com.bitwig.extension.callback.IntegerValueChangedCallback,int)

Registers an observer that reports the current scroll position, more specifically the position of the
 first channel within the underlying list of channels, that is shown as channel zero within the bank.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer number parameter
- `valueWhenUnassigned` (type int): a default value for the channel position that gets reported in case the channel bank is not
           connected to a list of channels in Bitwig Studio.

**Returns:** type void

### canScrollChannelsUp()

Value that reports if the channel bank can be scrolled further down.

**Returns:** type BooleanValue

### addCanScrollChannelsUpObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the channel bank can be scrolled further up.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter

**Returns:** type void

### canScrollChannelsDown()

Value that reports if the channel bank can be scrolled further down.

**Returns:** type BooleanValue

### addCanScrollChannelsDownObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the channel bank can be scrolled further down.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter

**Returns:** type void

### channelCount()

Value that reports the underlying total channel count (not the number of channels available in the bank
 window).

**Returns:** type IntegerValue

### addChannelCountObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the underlying total channel count (not the number of channels
 available in the bank window).

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer parameter

**Returns:** type void

### scrollSendsPageUp()

Scrolls the sends one page up.

**Returns:** type void

### scrollSendsPageDown()

Scrolls the sends one page down.

**Returns:** type void

### scrollSendsUp()

Scrolls the sends one step up.

**Returns:** type void

### scrollSendsDown()

Scrolls the sends one step down.

**Returns:** type void

### scrollToSend(int)

Scrolls to the send.

**Parameters:**
- `position` (type int): the index of the send.

**Returns:** type void

### addCanScrollSendsUpObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the sends window can be scrolled further up.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter

**Returns:** type void

### addCanScrollSendsDownObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the sends window can be scrolled further down.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter

**Returns:** type void

### addSendCountObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the underlying total send count (not the number of sends available in
 the bank window).

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

