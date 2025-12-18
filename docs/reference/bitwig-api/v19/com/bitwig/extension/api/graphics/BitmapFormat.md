# BitmapFormat

- Kind: enum
- Package: `com.bitwig.extension.api.graphics`

#### Values

- `ARGB32`: Each pixel is a 32-bit quantity, with alpha in the upper 8 bits, then red, then green, then
 blue. The 32-bit quantities are stored native-endian. Pre-multiplied alpha is used. (That is,
 50% transparent red is 0x80800000, not 0x80ff0000.)
- `RGB24_32`: Each pixel is a 32-bit quantity, with the upper 8 bits unused. Red, Green, and Blue are stored
 in the remaining 24 bits in that order.

## Methods

### values()

Returns an array containing the constants of this enum class, in
the order they are declared.

**Returns:** type BitmapFormat[] - an array containing the constants of this enum class, in the order they are declared

### valueOf(java.lang.String)

Returns the enum constant of this class with the specified name.
The string must match exactly an identifier used to declare an
enum constant in this class.  (Extraneous whitespace characters are 
not permitted.)

**Parameters:**
- `name` (type String): the name of the enum constant to be returned.

**Returns:** type BitmapFormat - the enum constant with the specified name

### bytesPerPixel()

**Returns:** type int

