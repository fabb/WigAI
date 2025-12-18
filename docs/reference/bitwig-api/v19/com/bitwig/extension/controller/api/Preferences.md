# Preferences

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

This interface is used to store custom controller settings into the Bitwig Studio preferences. The settings
 are shown to the user in the controller preferences dialog of Bitwig Studio.

**Extends:** `com.bitwig.extension.controller.api.Settings`

## Methods

### getSignalSetting(java.lang.String,java.lang.String,java.lang.String)

Returns a signal setting object, which is shown a push button with the given label in Bitwig Studio.

**Parameters:**
- `label` (type String): the name of the setting, must not be `null`
- `category` (type String): the name of the category, may not be `null`
- `action` (type String): the action string as displayed on the related Bitwig Studio button, must not be `null`

**Returns:** type Signal - the object that encapsulates the requested signal

### getNumberSetting(java.lang.String,java.lang.String,double,double,double,java.lang.String,double)

Returns a numeric setting that is shown a number field in Bitwig Studio.

**Parameters:**
- `label` (type String): the name of the setting, must not be `null`
- `category` (type String): the name of the category, may not be `null`
- `minValue` (type double): the minimum value that the user is allowed to enter
- `maxValue` (type double): the minimum value that the user is allowed to enter
- `stepResolution` (type double): the step resolution used for the number field
- `unit` (type String): the string that should be used to display the unit of the number
- `initialValue` (type double): the initial numeric value of the setting

**Returns:** type SettableRangedValue - the object that encapsulates the requested numeric setting

### getEnumSetting(java.lang.String,java.lang.String,java.lang.String[],java.lang.String)

Returns an enumeration setting that is shown either as a chooser or as a button group in Bitwig Studio,
 depending on the number of provided options.

**Parameters:**
- `label` (type String): the name of the setting, must not be `null`
- `category` (type String): the name of the category, may not be `null`
- `options` (type String[]): the string array that defines the allowed options for the button group or chooser
- `initialValue` (type String): the initial string value, must be one of the items specified with the option argument

**Returns:** type SettableEnumValue - the object that encapsulates the requested enum setting

### getEnumSetting(java.lang.String,java.lang.String,com.bitwig.extension.controller.api.EnumValueDefinition)

Returns an enumeration setting that is shown either as a chooser or as a button group in Bitwig Studio,
 depending on the number of provided options.

**Parameters:**
- `label` (type String): the name of the setting, must not be `null`
- `category` (type String): the name of the category, may not be `null`
- `initialValue` (type EnumValueDefinition): the initial string value, must be one of the items specified with the option argument

**Returns:** type SettableEnumValue - the object that encapsulates the requested enum setting

### getEnumSettingForValue(java.lang.String,java.lang.String,com.bitwig.extension.controller.api.SettableEnumValue)

Returns an enumeration setting that is shown either as a chooser or as a button group in Bitwig Studio,
 depending on the number of provided options.

**Parameters:**
- `label` (type String): the name of the setting, must not be `null`
- `category` (type String): the name of the category, may not be `null`
- `value` (type SettableEnumValue): the settable enum value that the setting will be connected to

**Returns:** type SettableEnumValue - the object that encapsulates the requested enum setting

### getStringSetting(java.lang.String,java.lang.String,int,java.lang.String)

Returns a textual setting that is shown as a text field in the Bitwig Studio user interface.

**Parameters:**
- `label` (type String): the name of the setting, must not be `null`
- `category` (type String): the name of the category, may not be `null`
- `numChars` (type int): the maximum number of character used for the text value
- `initialText` (type String): the initial text value of the setting

**Returns:** type SettableStringValue - the object that encapsulates the requested string setting

### getColorSetting(java.lang.String,java.lang.String,com.bitwig.extension.api.Color)

Returns a color setting that is shown in the Bitwig Studio user interface.

**Parameters:**
- `label` (type String): the name of the setting, must not be `null`
- `category` (type String): the name of the category, may not be `null`
- `initialColor` (type Color): the initial color value of the setting

**Returns:** type SettableColorValue - the object that encapsulates the requested string setting

### getColorSettingForValue(java.lang.String,java.lang.String,com.bitwig.extension.controller.api.SettableColorValue)

Returns a color setting that is shown in the Bitwig Studio user interface.

**Parameters:**
- `label` (type String): the name of the setting, must not be `null`
- `category` (type String): the name of the category, may not be `null`
- `value` (type SettableColorValue): the color value to which the setting will be connected to

**Returns:** type SettableColorValue - the object that encapsulates the requested string setting

### getBooleanSetting(java.lang.String,java.lang.String,boolean)

Returns a boolean setting.

**Parameters:**
- `label` (type String): the name of the setting, must not be `null`
- `category` (type String): the name of the category, may not be `null`
- `initialValue` (type boolean): the initial color value of the setting

**Returns:** type SettableBooleanValue - the object that encapsulates the requested string setting

### getBooleanSettingForValue(java.lang.String,java.lang.String,com.bitwig.extension.controller.api.SettableBooleanValue)

Returns an boolean setting.

**Parameters:**
- `label` (type String): the name of the setting, must not be `null`
- `category` (type String): the name of the category, may not be `null`
- `value` (type SettableBooleanValue): the settable enum value that the setting will be connected to

**Returns:** type SettableBooleanValue - the object that encapsulates the requested boolean setting

