# SpecificBitwigDevice

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Interface that can be used to access the parameter and output value of a specific Bitwig native device.

 Specific interfaces can be created by calling Device.createSpecificBitwigDevice(java.util.UUID).

## Methods

### createParameter(java.lang.String)

Creates a Parameter that will refer to the parameter of the device with the specified parameter
 id.

**Parameters:**
- `id` (type String)

**Returns:** type Parameter

### createIntegerOutputValue(java.lang.String)

Creates an IntegerValue that can be used to read a certain output value of the device.

**Parameters:**
- `id` (type String)

**Returns:** type IntegerValue

