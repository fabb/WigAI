# OscMessage

- Kind: interface
- Package: `com.bitwig.extension.api.opensoundcontrol`

An OSC message.

**Extends:** `com.bitwig.extension.api.opensoundcontrol.OscPacket`

## Methods

### getAddressPattern()

**Returns:** type String

### getTypeTag()

**Returns:** type String

### getArguments()

**Returns:** type List<Object>

### getString(int)

**Parameters:**
- `index` (type int)

**Returns:** type String

### getBlob(int)

**Parameters:**
- `index` (type int)

**Returns:** type byte[]

### getInt(int)

**Parameters:**
- `index` (type int)

**Returns:** type Integer

### getLong(int)

**Parameters:**
- `index` (type int)

**Returns:** type Long

### getFloat(int)

**Parameters:**
- `index` (type int)

**Returns:** type Float

### getDouble(int)

**Parameters:**
- `index` (type int)

**Returns:** type Double

### getBoolean(int)

**Parameters:**
- `index` (type int)

**Returns:** type Boolean

### getParentBundle()

If the message was part of a bundle, get a pointer back to it.
 If not, this methods returns null.

**Returns:** type OscBundle

