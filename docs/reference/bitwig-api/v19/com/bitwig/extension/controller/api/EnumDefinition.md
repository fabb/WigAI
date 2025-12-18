# EnumDefinition

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Defines an enumeration.

## Methods

### getValueCount()

Gets the number of entries in the enum, must be greater than 0.

**Returns:** type int

### valueDefinitionAt(int)

Gets the EnumValueDefinition for the given index.

**Parameters:**
- `valueIndex` (type int): must be in the range 0 ..

**Returns:** type EnumValueDefinition - null if not found

### valueDefinitionFor(java.lang.String)

Gets the EnumValueDefinition for the given enum id.

**Parameters:**
- `id` (type String)

**Returns:** type EnumValueDefinition - null if not found

