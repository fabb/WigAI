# Color

- Kind: class
- Package: `com.bitwig.extension.api`

This class represents an RGBA color with each component being stored as double.

**Extends:** java.lang.Object

## Methods

### fromRGB(double,double,double)

**Parameters:**
- `red` (type double)
- `green` (type double)
- `blue` (type double)

**Returns:** type Color

### fromRGBA(double,double,double,double)

**Parameters:**
- `red` (type double)
- `green` (type double)
- `blue` (type double)
- `alpha` (type double)

**Returns:** type Color

### fromRGB255(int,int,int)

**Parameters:**
- `red` (type int)
- `green` (type int)
- `blue` (type int)

**Returns:** type Color

### fromRGBA255(int,int,int,int)

**Parameters:**
- `red` (type int)
- `green` (type int)
- `blue` (type int)
- `alpha` (type int)

**Returns:** type Color

### fromHex(java.lang.String)

**Parameters:**
- `hex` (type String)

**Returns:** type Color

### mix(com.bitwig.extension.api.Color,com.bitwig.extension.api.Color,double)

Mixes two colors.

**Parameters:**
- `c1` (type Color)
- `c2` (type Color)
- `blend` (type double)

**Returns:** type Color

### toHex()

**Returns:** type String

### nullColor()

**Returns:** type Color

### blackColor()

**Returns:** type Color

### whiteColor()

**Returns:** type Color

### getRed()

**Returns:** type double

### getGreen()

**Returns:** type double

### getBlue()

**Returns:** type double

### getAlpha()

**Returns:** type double

### getRed255()

**Returns:** type int

### getGreen255()

**Returns:** type int

### getBlue255()

**Returns:** type int

### getAlpha255()

**Returns:** type int

### toHSV(double[])

**Parameters:**
- `hsv` (type double[]): array of length 3. On return, the array will be set to {h, s, v} with 0

**Returns:** type void

