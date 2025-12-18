# EnumValueDefinition

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Defines a single value from an enum.

## Methods

### enumDefinition()

Gets the enum definition this value belongs to.

**Returns:** type EnumDefinition

### getValueIndex()

Index of this value in the enum definition.

**Returns:** type int

### getId()

Identifier for this enum value. It will never change.
 This is the value to pass to SettableEnumValue.set(String).

**Returns:** type String

### getDisplayName()

This is a string that is suitable for display.

**Returns:** type String

### getLimitedDisplayName(int)

This is a shorter version of getDisplayName().

**Parameters:**
- `maxLength` (type int): Maximum number of characters

**Returns:** type String

