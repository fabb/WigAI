# ClipLauncherSlotBank

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent a scrollable fixed-size window that is connected to a section of the
 clip launcher slots for a specific track.

**Extends:** `com.bitwig.extension.controller.api.ClipLauncherSlotOrSceneBank`

## Methods

### select(int)

Selects the slot with the given index.

**Parameters:**
- `slot` (type int): the index of the slot within the slot window.

**Returns:** type void

### record(int)

Starts recording into the slot with the given index.

**Parameters:**
- `slot` (type int): the index of the slot within the slot window.

**Returns:** type void

### showInEditor(int)

Makes the clip content of the slot with the given index visible in the note or audio editor.

**Parameters:**
- `slot` (type int): the index of the slot within the slot window.

**Returns:** type void

### createEmptyClip(int,int)

Creates an new clip in the slot with the given index.

**Parameters:**
- `slot` (type int): the index of the slot within the slot window.
- `lengthInBeats` (type int)

**Returns:** type void

### deleteClip(int)

Deletes the clip in the slot with the given index.

**Parameters:**
- `slot` (type int): the index of the slot within the slot window.

**Returns:** type void

### duplicateClip(int)

Duplicates the clip in the slot with the given index.

**Parameters:**
- `slot` (type int): the index of the slot within the slot window.

**Returns:** type void

### addIsSelectedObserver(com.bitwig.extension.callback.IndexedBooleanValueChangedCallback)

Registers an observer that reports selection changes for the slots inside the window.

**Parameters:**
- `callback` (type IndexedBooleanValueChangedCallback): a callback function that receives two parameters: 1. the slot index (integer), and 2. a
           boolean parameter indicating if the slot at that index is selected (`true`) or not (`false`)

**Returns:** type void

### addHasContentObserver(com.bitwig.extension.callback.IndexedBooleanValueChangedCallback)

Registers an observer that reports which slots contain clips.

**Parameters:**
- `callback` (type IndexedBooleanValueChangedCallback): a callback function that receives two parameters: 1. the slot index (integer), and 2. a
           boolean parameter indicating if the slot at that index contains a clip (`true`) or not
           (`false`)

**Returns:** type void

### addPlaybackStateObserver(com.bitwig.extension.callback.ClipLauncherSlotBankPlaybackStateChangedCallback)

Registers an observer that reports the playback state of clips / slots. The reported states include
 `stopped`, `playing`, `recording`, but also `queued for stop`, `queued for playback`, `queued for
 recording`.

**Parameters:**
- `callback` (type ClipLauncherSlotBankPlaybackStateChangedCallback): a callback function that receives three parameters: 1. the slot index (integer), 2. the queued
           or playback state: `0` when stopped, `1` when playing, or `2` when recording, and 3. a boolean
           parameter indicating if the second argument is referring to the queued state (`true`) or the
           actual playback state (`false`)

**Returns:** type void

### addIsPlayingObserver(com.bitwig.extension.callback.IndexedBooleanValueChangedCallback)

Registers an observer that reports which slots have clips that are currently playing.

**Parameters:**
- `callback` (type IndexedBooleanValueChangedCallback): a callback function that receives two parameters: 1. the slot index (integer), and 2. a
           boolean parameter indicating if the slot at that index has a clip that is currently playing
           (`true`) or not (`false`)

**Returns:** type void

### addIsRecordingObserver(com.bitwig.extension.callback.IndexedBooleanValueChangedCallback)

Registers an observer that reports which slots have clips that are currently recording.

**Parameters:**
- `callback` (type IndexedBooleanValueChangedCallback): a callback function that receives two parameters: 1. the slot index (integer), and 2. a
           boolean parameter indicating if the slot at that index has a clip that is currently recording
           (`true`) or not (`false`)

**Returns:** type void

### addIsPlaybackQueuedObserver(com.bitwig.extension.callback.IndexedBooleanValueChangedCallback)

Add an observer if clip playback is queued on the slot.

**Parameters:**
- `callback` (type IndexedBooleanValueChangedCallback): a callback function that receives two parameters: 1. the slot index (integer), and 2. a
           boolean parameter indicating if the slot at that index has a clip that is currently queued for
           playback (`true`) or not (`false`)

**Returns:** type void

### addIsRecordingQueuedObserver(com.bitwig.extension.callback.IndexedBooleanValueChangedCallback)

Add an observer if clip recording is queued on the slot.

**Parameters:**
- `callback` (type IndexedBooleanValueChangedCallback): a callback function that receives two parameters: 1. the slot index (integer), and 2. a
           boolean parameter indicating if the slot at that index has a clip that is currently queued for
           recording (`true`) or not (`false`)

**Returns:** type void

### addIsStopQueuedObserver(com.bitwig.extension.callback.IndexedBooleanValueChangedCallback)

Add an observer if clip playback is queued to stop on the slot.

**Parameters:**
- `callback` (type IndexedBooleanValueChangedCallback): a callback function that receives two parameters: 1. the slot index (integer), and 2. a
           boolean parameter indicating if the slot at that index has a clip that is currently queued for
           stop (`true`) or not (`false`)

**Returns:** type void

### addIsQueuedObserver(com.bitwig.extension.callback.IndexedBooleanValueChangedCallback)

**Parameters:**
- `callback` (type IndexedBooleanValueChangedCallback)

**Returns:** type void

### addColorObserver(com.bitwig.extension.callback.IndexedColorValueChangedCallback)

Registers an observer that reports the colors of clip in the current slot window.

**Parameters:**
- `callback` (type IndexedColorValueChangedCallback): a callback function that receives four parameters: 1. the slot index (integer), 2. the red
           coordinate of the RBG color value, 3. the green coordinate of the RBG color value, and 4. the
           blue coordinate of the RBG color value

**Returns:** type void

### setIndication(boolean)

Specifies if the Bitwig Studio clip launcher should indicate which slots are part of the window. By
 default indications are disabled.

**Parameters:**
- `shouldIndicate` (type boolean): `true` if visual indications should be enabled, `false` otherwise

**Returns:** type void

### isMasterTrackContentShownOnTrackGroups()

Returns an object that can be used to observe and toggle if the slots on a connected track group show
 either scenes launch buttons (for launching the content of the track group) or the clips of the group
 master track.

**Returns:** type SettableBooleanValue - a boolean value object.

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

