# CursorDevice

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

A special kind of selection cursor used for devices.

**Extends:** `com.bitwig.extension.controller.api.Cursor`, `com.bitwig.extension.controller.api.Device`

## Methods

### getChannel()

Returns the channel that this cursor device was created on. Currently this will always be a track or
 cursor track instance.

**Returns:** type Channel - the track or cursor track object that was used for creation of this cursor device.

### channel()

Returns the channel that this cursor device was created on. Currently this will always be a track or
 cursor track instance.

**Returns:** type Channel - the track or cursor track object that was used for creation of this cursor device.

### selectParent()

Selects the parent device if there is any.

**Returns:** type void

### selectDevice(com.bitwig.extension.controller.api.Device)

Moves this cursor to the given device.

**Parameters:**
- `device` (type Device): the device that this cursor should point to

**Returns:** type void

### selectFirstInChannel(com.bitwig.extension.controller.api.Channel)

Selects the first device in the given channel.

**Parameters:**
- `channel` (type Channel): the channel in which the device should be selected

**Returns:** type void

### selectLastInChannel(com.bitwig.extension.controller.api.Channel)

Selects the last device in the given channel.

**Parameters:**
- `channel` (type Channel): the channel in which the device should be selected

**Returns:** type void

### selectFirstInSlot(java.lang.String)

Selects the first device in the nested FX slot with the given name.

**Parameters:**
- `chain` (type String): the name of the FX slot in which the device should be selected

**Returns:** type void

### selectLastInSlot(java.lang.String)

Selects the last device in the nested FX slot with the given name.

**Parameters:**
- `chain` (type String): the name of the FX slot in which the device should be selected

**Returns:** type void

### selectFirstInKeyPad(int)

Selects the first device in the drum pad associated with the given key.

**Parameters:**
- `key` (type int): the key associated with the drum pad in which the device should be selected

**Returns:** type void

### selectLastInKeyPad(int)

Selects the last device in the drum pad associated with the given key.

**Parameters:**
- `key` (type int): the key associated with the drum pad in which the device should be selected

**Returns:** type void

### selectFirstInLayer(int)

Selects the first device in the nested layer with the given index.

**Parameters:**
- `index` (type int): the index of the nested layer in which the device should be selected

**Returns:** type void

### selectLastInLayer(int)

Selects the last device in the nested layer with the given index.

**Parameters:**
- `index` (type int): the index of the nested layer in which the device should be selected

**Returns:** type void

### selectFirstInLayer(java.lang.String)

Selects the first device in the nested layer with the given name.

**Parameters:**
- `name` (type String): the name of the nested layer in which the device should be selected

**Returns:** type void

### selectLastInLayer(java.lang.String)

Selects the last device in the nested layer with the given name.

**Parameters:**
- `name` (type String): the name of the nested layer in which the device should be selected

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

### getDeviceChain()

Returns a representation of the device chain that contains this device. Possible device chain instances
 are tracks, device layers, drums pads, or FX slots.

**Returns:** type DeviceChain - the requested device chain object

### deviceChain()

Returns a representation of the device chain that contains this device. Possible device chain instances
 are tracks, device layers, drums pads, or FX slots.

**Returns:** type DeviceChain - the requested device chain object

### position()

Value that reports the position of the device within the parent device chain.

**Returns:** type IntegerValue

### addPositionObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the position of the device within the parent device chain.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer parameter

**Returns:** type void

### isWindowOpen()

Returns an object that provides access to the open state of plugin windows.

**Returns:** type SettableBooleanValue - a boolean value object that represents the open state of the editor window, in case the device
         features a custom editor window (such as plugins).

### isExpanded()

Returns an object that provides access to the expanded state of the device.

**Returns:** type SettableBooleanValue - a boolean value object that represents the expanded state of the device.

### isMacroSectionVisible()

Returns an object that provides access to the visibility of the device macros section.

**Returns:** type SettableBooleanValue - a boolean value object that represents the macro section visibility.

### isRemoteControlsSectionVisible()

Returns an object that provides access to the visibility of the device remote controls section.

**Returns:** type SettableBooleanValue - a boolean value object that represents the remote controls section visibility.

### isParameterPageSectionVisible()

Returns an object that provides access to the visibility of the parameter page mapping editor.

**Returns:** type SettableBooleanValue - a boolean value object that represents visibility of the parameter page mapping editor.

### getParameter(int)

Returns the parameter with the given index in the current parameter page.

**Parameters:**
- `indexInPage` (type int): the index of the parameter within the current parameter page.

**Returns:** type Parameter - an object that provides access to the requested parameter

### createCursorRemoteControlsPage(int)

Creates a cursor for the selected remote controls page in the device with the supplied number of
 parameters. This section will follow the current page selection made by the user in the application.

**Parameters:**
- `parameterCount` (type int): The number of parameters the remote controls should contain

**Returns:** type CursorRemoteControlsPage

### createCursorRemoteControlsPage(java.lang.String,int,java.lang.String)

Creates a cursor for a remote controls page in the device with the supplied number of parameters. This
 section will be independent from the current page selected by the user in Bitwig Studio's user
 interface. The supplied filter is an expression that can be used to match pages this section is
 interested in. The expression is matched by looking at the tags added to the pages. If the expression is
 empty then no filtering will occur.

**Parameters:**
- `name` (type String): A name to associate with this section. This will be used to remember manual mappings made by
           the user within this section.
- `parameterCount` (type int): The number of parameters the remote controls should contain
- `filterExpression` (type String): An expression used to match pages that the user can navigate through. For now this can only be
           the name of a single tag the pages should contain (e.g "drawbars", "dyn", "env", "eq",
           "filter", "fx", "lfo", "mixer", "osc", "overview", "perf").

**Returns:** type CursorRemoteControlsPage

### getEnvelopeParameter(int)

Returns the parameter with the given index in the envelope parameter page.

**Parameters:**
- `index` (type int): the index of the parameter within the envelope parameter page.

**Returns:** type Parameter - an object that provides access to the requested parameter

### getCommonParameter(int)

Returns the parameter with the given index in the common parameter page.

**Parameters:**
- `index` (type int): the index of the parameter within the common parameter page.

**Returns:** type Parameter - an object that provides access to the requested parameter

### getModulationSource(int)

Returns the modulation source at the given index.

**Parameters:**
- `index` (type int): the index of the modulation source

**Returns:** type ModulationSource - An object that represents the requested modulation source

### getMacro(int)

Returns the macro control at the given index.

**Parameters:**
- `index` (type int): the index of the macro control, must be in the range [0..7]

**Returns:** type Macro - An object that represents the requested macro control

### addHasSelectedDeviceObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device is selected.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### selectInEditor()

Selects the device in Bitwig Studio.

**Returns:** type void

### isPlugin()

Value that reports if the device is a plugin.

**Returns:** type BooleanValue

### addIsPluginObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device is a plugin.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### previousParameterPage()

Switches to the previous parameter page.

**Returns:** type void

### nextParameterPage()

Switches to the next parameter page.

**Returns:** type void

### addPreviousParameterPageEnabledObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if there is a previous parameter page.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter

**Returns:** type void

### addNextParameterPageEnabledObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if there is a next parameter page.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter

**Returns:** type void

### setParameterPage(int)

Switches to the parameter page at the given page index.

**Parameters:**
- `page` (type int): the index of the desired parameter page

**Returns:** type void

### switchToPreviousPreset()

Loads the previous preset.

**Returns:** type void

### switchToNextPreset()

Loads the next preset.

**Returns:** type void

### switchToPreviousPresetCategory()

Switches to the previous preset category.

**Returns:** type void

### switchToNextPresetCategory()

Switches to the next preset category.

**Returns:** type void

### switchToPreviousPresetCreator()

Switches to the previous preset creator.

**Returns:** type void

### switchToNextPresetCreator()

Switches to the next preset creator.

**Returns:** type void

### createDeviceBrowser(int,int)

Returns an object used for browsing devices, presets and other content. Committing the browsing session
 will load or create a device from the selected resource and replace the current device.

**Parameters:**
- `numFilterColumnEntries` (type int): the size of the window used to navigate the filter column entries.
- `numResultsColumnEntries` (type int): the size of the window used to navigate the results column entries.

**Returns:** type Browser - the requested device browser object.

### name()

Value that reports the name of the device.

**Returns:** type StringValue

### addNameObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the name of the device.

**Parameters:**
- `len` (type int): the maximum length of the name. Longer names will get truncated.
- `textWhenUnassigned` (type String): the default name that gets reported when the device is not associated with a Bitwig Studio
           device yet.
- `callback` (type StringValueChangedCallback): a callback function that receives a single name (string) parameter

**Returns:** type void

### presetName()

Value that reports the last loaded preset name.

**Returns:** type StringValue

### addPresetNameObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the last loaded preset name.

**Parameters:**
- `len` (type int): the maximum length of the name. Longer names will get truncated.
- `textWhenUnassigned` (type String): the default name that gets reported when the device is not associated with a Bitwig Studio
           device yet.
- `callback` (type StringValueChangedCallback): a callback function that receives a single name (string) parameter

**Returns:** type void

### presetCategory()

Value that reports the current preset category name.

**Returns:** type StringValue

### addPresetCategoryObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the current preset category name.

**Parameters:**
- `len` (type int): the maximum length of the name. Longer names will get truncated.
- `textWhenUnassigned` (type String): the default name that gets reported when the device is not associated with a Bitwig Studio
           device yet.
- `callback` (type StringValueChangedCallback): a callback function that receives a single name (string) parameter

**Returns:** type void

### presetCreator()

Value that reports the current preset creator name.

**Returns:** type StringValue

### addPresetCreatorObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the current preset creator name.

**Parameters:**
- `len` (type int): the maximum length of the name. Longer names will get truncated.
- `textWhenUnassigned` (type String): the default name that gets reported when the device is not associated with a Bitwig Studio
           device yet.
- `callback` (type StringValueChangedCallback): a callback function that receives a single name (string) parameter

**Returns:** type void

### addSelectedPageObserver(int,com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the currently selected parameter page.

**Parameters:**
- `valueWhenUnassigned` (type int): the default page index that gets reported when the device is not associated with a device
           instance in Bitwig Studio yet.
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single page index parameter (integer)

**Returns:** type void

### addActiveModulationSourceObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the name of the active modulation source.

**Parameters:**
- `len` (type int): the maximum length of the name. Longer names will get truncated.
- `textWhenUnassigned` (type String): the default name that gets reported when the device is not associated with a Bitwig Studio
           device yet.
- `callback` (type StringValueChangedCallback): a callback function that receives a single name parameter (string)

**Returns:** type void

### addPageNamesObserver(com.bitwig.extension.callback.StringArrayValueChangedCallback)

Registers an observer that reports the names of the devices parameter pages.

**Parameters:**
- `callback` (type StringArrayValueChangedCallback): a callback function that receives a single string array parameter containing the names of the
           parameter pages

**Returns:** type void

### addPresetNamesObserver(com.bitwig.extension.callback.StringArrayValueChangedCallback)

Registers an observer that reports the names of the available presets for the device according to the
 current configuration of preset category and creator filtering.

**Parameters:**
- `callback` (type StringArrayValueChangedCallback): a callback function that receives a single string array parameter containing the names of the
           presets for the current category and creator filter.

**Returns:** type void

### loadPreset(int)

Loads the preset with the index from the list provided by addPresetNamesObserver(com.bitwig.extension.callback.StringArrayValueChangedCallback).

**Parameters:**
- `index` (type int)

**Returns:** type void

### addPresetCategoriesObserver(com.bitwig.extension.callback.StringArrayValueChangedCallback)

Registers an observer that reports the names of the available preset categories for the device.

**Parameters:**
- `callback` (type StringArrayValueChangedCallback): a callback function that receives a single string array parameter containing the names of the
           preset categories

**Returns:** type void

### setPresetCategory(int)

Sets the preset category filter with the index from the array provided by
 addPresetCategoriesObserver(com.bitwig.extension.callback.StringArrayValueChangedCallback).

**Parameters:**
- `index` (type int)

**Returns:** type void

### addPresetCreatorsObserver(com.bitwig.extension.callback.StringArrayValueChangedCallback)

Registers an observer that reports the names of the available preset creators for the device.

**Parameters:**
- `callback` (type StringArrayValueChangedCallback): a callback function that receives a single string array parameter containing the names of the
           preset creators

**Returns:** type void

### setPresetCreator(int)

Sets the preset creator filter with the index from the list provided by
 addPresetCreatorsObserver(com.bitwig.extension.callback.StringArrayValueChangedCallback).

**Parameters:**
- `index` (type int)

**Returns:** type void

### toggleEnabledState()

Toggles the enabled state of the device.

**Returns:** type void

### isEnabled()

Value that reports if the device is enabled.

**Returns:** type SettableBooleanValue

### addIsEnabledObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the device is enabled.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter

**Returns:** type void

### hasSlots()

Indicates if the device has nested device chain slots. Use slotNames() to get a list of
 available slot names, and navigate to devices in those slots using the CursorDevice interface.

**Returns:** type BooleanValue - a value object that indicates if the device has nested device chains in FX slots.

### slotNames()

Value of the list of available FX slots in this device.

**Returns:** type StringArrayValue

### addSlotsObserver(com.bitwig.extension.callback.StringArrayValueChangedCallback)

Registers an observer that gets notified when the list of available FX slots changes.

**Parameters:**
- `callback` (type StringArrayValueChangedCallback): a callback function which takes a single string array argument that contains the names of the
           slots.

**Returns:** type void

### getCursorSlot()

Returns an object that represents the selected device slot as shown in the user interface, and that
 provides access to the contents of slot's device chain.

**Returns:** type DeviceSlot - the requested slot cursor object

### isNested()

Indicates if the device is contained by another device.

**Returns:** type BooleanValue - a value object that indicates if the device is nested

### hasLayers()

Indicates if the device supports nested layers.

**Returns:** type BooleanValue - a value object that indicates if the device supports nested layers.

### hasDrumPads()

Indicates if the device has individual device chains for each note value.

**Returns:** type BooleanValue - a value object that indicates if the device has individual device chains for each note value.

### createLayerBank(int)

Create a bank for navigating the nested layers of the device using a fixed-size window.

 This bank will work over the following devices:
  - Instrument Layer
  - Effect Layer
  - Instrument Selector
  - Effect Selector

**Parameters:**
- `numChannels` (type int): the number of channels that the device layer bank should be configured with

**Returns:** type DeviceLayerBank - a device layer bank object configured with the desired number of channels

### createDrumPadBank(int)

Create a bank for navigating the nested layers of the device using a fixed-size window.

**Parameters:**
- `numPads` (type int): the number of channels that the drum pad bank should be configured with

**Returns:** type DrumPadBank - a drum pad bank object configured with the desired number of pads

### createCursorLayer()

Returns a device layer instance that can be used to navigate the layers or drum pads of the device, in
 case it has any

 This is the selected layer from the user interface.

**Returns:** type CursorDeviceLayer - a cursor device layer instance

### createChainSelector()

Creates a ChainSelector object which will give you control over the current device if it is an
 Instrument Selector or an Effect Selector.

 To check if the device is currently a ChainSelector, use 

invalid @link
{@link ChainSelector.exists()

}.

 If you want to have access to all the chains, use createLayerBank(int).

**Returns:** type ChainSelector - a chain selector instance

### createSpecificBitwigDevice(java.util.UUID)

Creates an interface for accessing the features of a specific Bitwig device.

**Parameters:**
- `deviceId` (type UUID)

**Returns:** type SpecificBitwigDevice

### createSpecificVst2Device(int)

Creates an interface for accessing the features of a specific VST2 device.

**Parameters:**
- `deviceId` (type int)

**Returns:** type SpecificPluginDevice

### createSpecificVst2Device(int...)

Creates an interface for accessing the features of a specific VST2 device.

**Parameters:**
- `deviceIds` (type int...)

**Returns:** type SpecificPluginDevice

### createSpecificVst3Device(java.lang.String)

Creates an interface for accessing the features of a specific VST2 device.

**Parameters:**
- `deviceId` (type String)

**Returns:** type SpecificPluginDevice

### createSpecificVst3Device(java.lang.String...)

Creates an interface for accessing the features of a specific VST2 device.

**Parameters:**
- `deviceIds` (type String...)

**Returns:** type SpecificPluginDevice

### addDirectParameterIdObserver(com.bitwig.extension.callback.StringArrayValueChangedCallback)

Adds an observer on a list of all parameters for the device.

 The callback always updates with an array containing all the IDs for the device.

**Parameters:**
- `callback` (type StringArrayValueChangedCallback): function with the signature (String[])

**Returns:** type void

### addDirectParameterNameObserver(int,com.bitwig.extension.callback.DirectParameterNameChangedCallback)

Adds an observer for the parameter names (initial and changes) of all parameters for the device.

**Parameters:**
- `maxChars` (type int): maximum length of the string sent to the observer.
- `callback` (type DirectParameterNameChangedCallback): function with the signature (String ID, String name)

**Returns:** type void

### addDirectParameterValueDisplayObserver(int,com.bitwig.extension.callback.DirectParameterDisplayedValueChangedCallback)

Returns an observer that reports changes of parameter display values, i.e. parameter values formatted as
 a string to be read by the user, for example "-6.02 dB". The returned observer object can be used to
 configure which parameters should be observed. By default no parameters are observed. It should be
 avoided to observe all parameters at the same time for performance reasons.

**Parameters:**
- `maxChars` (type int): maximum length of the string sent to the observer.
- `callback` (type DirectParameterDisplayedValueChangedCallback): function with the signature (String ID, String valueDisplay)

**Returns:** type DirectParameterValueDisplayObserver - an observer object that can be used to enable or disable actual observing for certain
         parameters.

### addDirectParameterNormalizedValueObserver(com.bitwig.extension.callback.DirectParameterNormalizedValueChangedCallback)

Adds an observer for the parameter display value (initial and changes) of all parameters for the device.

**Parameters:**
- `callback` (type DirectParameterNormalizedValueChangedCallback): a callback function with the signature (String ID, float normalizedValue). If the value is not
           accessible 'Number.NaN' (not-a-number) is reported, can be checked with 'isNaN(value)'.

**Returns:** type void

### setDirectParameterValueNormalized(java.lang.String,java.lang.Number,java.lang.Number)

Sets the parameter with the specified `id` to the given `value` according to the given `resolution`.

**Parameters:**
- `id` (type String): the parameter identifier string
- `value` (type Number): the new value normalized to the range [0..resolution-1]
- `resolution` (type Number): the resolution of the new value

**Returns:** type void

### incDirectParameterValueNormalized(java.lang.String,java.lang.Number,java.lang.Number)

Increases the parameter with the specified `id` by the given `increment` according to the given
 `resolution`. To decrease the parameter value pass in a negative increment.

**Parameters:**
- `id` (type String): the parameter identifier string
- `increment` (type Number): the amount that the parameter value should be increased by, normalized to the range
           [0..resolution-1]
- `resolution` (type Number): the resolution of the new value

**Returns:** type void

### sampleName()

Value that reports the file name of the currently loaded sample, in case the device is a sample
 container device.

**Returns:** type StringValue

### addSampleNameObserver(int,java.lang.String,com.bitwig.extension.callback.StringValueChangedCallback)

Registers an observer that reports the file name of the currently loaded sample, in case the device is a
 sample container device.

**Parameters:**
- `maxChars` (type int): maximum length of the string sent to the observer.
- `textWhenUnassigned` (type String): the default name that gets reported when the device is not associated with a Bitwig Studio
           device yet.
- `callback` (type StringValueChangedCallback): a callback function that receives a single string parameter.

**Returns:** type void

### createSiblingsDeviceBank(int)

Returns an object that provides bank-wise navigation of sibling devices of the same device chain
 (including the device instance used to create the siblings bank).

**Parameters:**
- `numDevices` (type int): the number of devices that are simultaneously accessible

**Returns:** type DeviceBank - the requested device bank object

### browseToInsertBeforeDevice()

Starts browsing for content that can be inserted before this device in Bitwig Studio's popup browser.

**Returns:** type void

### browseToInsertAfterDevice()

Starts browsing for content that can be inserted before this device in Bitwig Studio's popup browser.

**Returns:** type void

### browseToReplaceDevice()

Starts browsing for content that can replace this device in Bitwig Studio's popup browser.

**Returns:** type void

### afterDeviceInsertionPoint()

InsertionPoint that can be used for inserting after this device.

**Returns:** type InsertionPoint

### beforeDeviceInsertionPoint()

InsertionPoint that can be used for inserting before this device.

**Returns:** type InsertionPoint

### replaceDeviceInsertionPoint()

InsertionPoint that can be used for replacing this device.

**Returns:** type InsertionPoint

### deviceType()

The type of this device.

**Returns:** type EnumValue

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

