# Scene

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent scenes in Bitwig Studio.

**Extends:** `com.bitwig.extension.controller.api.ClipLauncherSlotOrScene`

## Methods

### getName()

Returns an object that provides access to the name of the scene.

**Returns:** type SettableStringValue - a string value object that represents the scene name.

### name()

Returns an object that provides access to the name of the scene.

**Returns:** type SettableStringValue - a string value object that represents the scene name.

### clipCount()

Value that reports the number of clips in the scene.

**Returns:** type IntegerValue

### addClipCountObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the number of clips in the scene.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer parameter

**Returns:** type void

### addPositionObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports the position of the scene within the list of Bitwig Studio scenes.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): a callback function that receives a single integer parameter

**Returns:** type void

### addIsSelectedInEditorObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the scene is selected in Bitwig Studio.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that takes a single boolean parameter.

**Returns:** type void

### selectInEditor()

Selects the scene in Bitwig Studio.

**Returns:** type void

### showInEditor()

Makes the scene visible in the Bitwig Studio user interface.

**Returns:** type void

### launch()

Launches the clip or scene.

**Returns:** type void

### launchAction()

**Returns:** type HardwareActionBindable

### launchAlt()

Launches with alternative settings.

**Returns:** type void

### launchAltAction()

**Returns:** type HardwareActionBindable

### launchRelease()

Call it when the pad is released.

**Returns:** type void

### launchReleaseAction()

**Returns:** type HardwareActionBindable

### launchReleaseAlt()

Call it when the pad is released with alternative settings.

**Returns:** type void

### launchReleaseAltAction()

**Returns:** type HardwareActionBindable

### launchWithOptions(java.lang.String,java.lang.String)

Launches with the given options:

**Parameters:**
- `quantization` (type String): possible values are "default", "none", "8", "4", "2", "1", "1/2", "1/4", "1/8", "1/16"
- `launchMode` (type String): possible values are: "default", "from_start", "continue_or_from_start",
           "continue_or_synced", "synced"

**Returns:** type void

### launchWithOptionsAction(java.lang.String,java.lang.String)

**Parameters:**
- `quantization` (type String)
- `launchMode` (type String)

**Returns:** type HardwareActionBindable

### launchLastClipWithOptions(java.lang.String,java.lang.String)

Launches the last clip with the given options:

**Parameters:**
- `quantization` (type String): possible values are "default", "none", "8", "4", "2", "1", "1/2", "1/4", "1/8", "1/16"
- `launchMode` (type String): possible values are: "default", "from_start", "continue_or_from_start",
           "continue_or_synced", "synced"

**Returns:** type void

### launchLastClipWithOptionsAction(java.lang.String,java.lang.String)

**Parameters:**
- `quantization` (type String)
- `launchMode` (type String)

**Returns:** type HardwareActionBindable

### sceneIndex()

Value that reports the position of the scene within the list of Bitwig Studio scenes.

**Returns:** type IntegerValue

### copyFrom(com.bitwig.extension.controller.api.ClipLauncherSlotOrScene)

Copies the current slot or scene into the dest slot or scene.

**Parameters:**
- `source` (type ClipLauncherSlotOrScene)

**Returns:** type void

### moveTo(com.bitwig.extension.controller.api.ClipLauncherSlotOrScene)

Moves the current slot or scene into the destination slot or scene.

**Parameters:**
- `dest` (type ClipLauncherSlotOrScene)

**Returns:** type void

### color()

Value that reports the color of this slot.

**Returns:** type SettableColorValue

### setIndication(boolean)

Specifies if the Bitwig Studio clip launcher should indicate which slots and scenes are part of the
 window. By default indications are disabled.

**Parameters:**
- `shouldIndicate` (type boolean): `true` if visual indications should be enabled, `false` otherwise

**Returns:** type void

### replaceInsertionPoint()

An InsertionPoint that is used to replace the contents of this slot or scene.

**Returns:** type InsertionPoint

### nextSceneInsertionPoint()

An InsertionPoint that can be used to insert content in the next scene.

**Returns:** type InsertionPoint

### previousSceneInsertionPoint()

An InsertionPoint that can be used to insert content after this slot or scene.

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

