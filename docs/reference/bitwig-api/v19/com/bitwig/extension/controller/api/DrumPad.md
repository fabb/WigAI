# DrumPad

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface are special kind of channel objects that represent the pads of a drum machine
 instrument. Drum pads are typically associated to channel objects via note keys.

**Extends:** `com.bitwig.extension.controller.api.Channel`

## Methods

### insertionPoint()

InsertionPoint that can be used to insert content in this drum pad.

**Returns:** type InsertionPoint

### channelId()

Reports the channel UUID.

**Returns:** type StringValue

### channelIndex()

Reports the channel index.

**Returns:** type IntegerValue

### isActivated()

Returns an object that represents the activated state of the channel.

**Returns:** type SettableBooleanValue - an object that provides access to the channels activated state.

### getVolume()

Gets a representation of the channels volume control.

**Returns:** type Parameter - an object that provides access to the channels volume control.

### volume()

Gets a representation of the channels volume control.

**Returns:** type Parameter - an object that provides access to the channels volume control.

### getPan()

Gets a representation of the channels pan control.

**Returns:** type Parameter - an object that provides access to the channels pan control.

### pan()

Gets a representation of the channels pan control.

**Returns:** type Parameter - an object that provides access to the channels pan control.

### getMute()

Gets a representation of the channels mute control.

**Returns:** type SettableBooleanValue - an object that provides access to the channels mute control.

### mute()

Gets a representation of the channels mute control.

**Returns:** type SettableBooleanValue - an object that provides access to the channels mute control.

### getSolo()

Gets a representation of the channels solo control.

**Returns:** type SoloValue - an object that provides access to the channels solo control.

### solo()

Gets a representation of the channels solo control.

**Returns:** type SoloValue - an object that provides access to the channels solo control.

### isMutedBySolo()

True if the current channel is being muted by an other channel with solo on.

**Returns:** type BooleanValue

### addVuMeterObserver(int,int,boolean,com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer for the VU-meter of this track.

**Parameters:**
- `range` (type int): the number of steps to which the reported values should be scaled. For example a range of 128
           would cause the callback to be called with values between 0 and 127.
- `channel` (type int): 0 for left channel, 1 for right channel, -1 for the sum of both
- `peak` (type boolean): when `true` the peak value is reported, otherwise the RMS value
- `callback` (type IntegerValueChangedCallback): a callback function that takes a single numeric argument. The value is in the range
           [0..range-1].

**Returns:** type void

### addNoteObserver(com.bitwig.extension.callback.NotePlaybackCallback)

Registers an observer that reports notes when they are played on the channel.

**Parameters:**
- `callback` (type NotePlaybackCallback): a callback function that receives three parameters: 1. on/off state (boolean), 2. key (int),
           and 3. velocity (float).

**Returns:** type void

### playingNotes()

Returns an array of the playing notes.

**Returns:** type PlayingNoteArrayValue

### addColorObserver(com.bitwig.extension.callback.ColorValueChangedCallback)

Registers an observer that receives notifications about the color of the channel. The callback gets
 called at least once immediately after this function call to report the current color. Additional calls
 are fired each time the color changes.

**Parameters:**
- `callback` (type ColorValueChangedCallback): a callback function that receives three float parameters in the range [0..1]: 1. red, 2.
           green, and 3. blue.

**Returns:** type void

### color()

Get the color of the channel.

**Returns:** type SettableColorValue

### sendBank()

Gets a SendBank that can be used to navigate the sends of this channel.

**Returns:** type SendBank

### getSend(int)

Gets a representation of the channels send control at the given index.

**Parameters:**
- `index` (type int): the index of the send, must be valid

**Returns:** type Send - an object that provides access to the requested send control.

### duplicate()

Duplicates the track.

**Returns:** type void

### selectInMixer()

Selects the device chain in the Bitwig Studio mixer, in case it is a selectable object.

**Returns:** type void

### addIsSelectedInMixerObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device chain is selected in Bitwig Studio mixer.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter.

**Returns:** type void

### makeVisibleInArranger()

Tries to scroll the contents of the arrangement editor so that the channel becomes visible.

**Returns:** type void

### makeVisibleInMixer()

Tries to scroll the contents of the mixer panel so that the channel becomes visible.

**Returns:** type void

### selectInEditor()

Selects the device chain in Bitwig Studio, in case it is a selectable object.

**Returns:** type void

### name()

Value that reports the name of the device chain, such as the track name or the drum pad
 name.

**Returns:** type SettableStringValue

### addNameObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the name of the device chain, such as the track name or the drum pad
 name.

**Parameters:**
- `numChars` (type int): the maximum number of characters used for the reported name
- `textWhenUnassigned` (type String): the default text that gets reported when the device chain is not associated with an object in
           Bitwig Studio yet.
- `callback` (type StringValueChangedCallback): a callback function that receives a single name parameter (string).

**Returns:** type void

### addIsSelectedInEditorObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device chain is selected in Bitwig Studio editors.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter.

**Returns:** type void

### addIsSelectedObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

**Parameters:**
- `callback` (type BooleanValueChangedCallback)

**Returns:** type void

### createDeviceBank(int)

Returns an object that provides bank-wise navigation of devices.

**Parameters:**
- `numDevices` (type int): the number of devices should be accessible simultaneously

**Returns:** type DeviceBank - the requested device bank object

### createDeviceBrowser(int,int)

Returns an object used for browsing devices, presets and other content. Committing the browsing session
 will load or create a device from the selected resource and insert it into the device chain.

**Parameters:**
- `numFilterColumnEntries` (type int): the size of the window used to navigate the filter column entries.
- `numResultsColumnEntries` (type int): the size of the window used to navigate the results column entries.

**Returns:** type Browser - the requested device browser object.

### select()

**Returns:** type void

### browseToInsertAtStartOfChain()

Starts browsing for content that can be inserted at the start of this device chain.

**Returns:** type void

### browseToInsertAtEndOfChain()

Starts browsing for content that can be inserted at the end of this device chain.

**Returns:** type void

### startOfDeviceChainInsertionPoint()

InsertionPoint that can be used to insert at the start of the device chain.

**Returns:** type InsertionPoint

### endOfDeviceChainInsertionPoint()

InsertionPoint that can be used to insert at the end of the device chain.

**Returns:** type InsertionPoint

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

### deleteObject()

Deletes this object from the document.

 If you want to delete multiple objects at once, see Host.deleteObjects().

**Returns:** type void

### deleteObjectAction()

Deletes this object from the document.

**Returns:** type HardwareActionBindable

### duplicateObject()

Duplicates this object into the document.

 If you want to duplicate multiple objects at once, see Host.duplicateObjects().

**Returns:** type void

### duplicateObjectAction()

Duplicates this object into the document.

**Returns:** type HardwareActionBindable

