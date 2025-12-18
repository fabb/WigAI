# Host

- Kind: interface
- Package: `com.bitwig.extension`

Defines the interface through which an extension can talk to the host application.

## Methods

### getHostApiVersion()

Returns the latest supported API version of the host application.

**Returns:** type int - the latest supported API version of the host application

### getHostVendor()

Returns the vendor of the host application.

**Returns:** type String - the vendor of the host application

### getHostProduct()

Returns the product name of the host application.

**Returns:** type String - the product name of the host application

### getHostVersion()

Returns the version number of the host application.

**Returns:** type String - the version number of the host application

### getPlatformType()

The platform type that this host is running on.

**Returns:** type PlatformType

### setErrorReportingEMail(java.lang.String)

Sets an email address to use for reporting errors found in this script.

**Parameters:**
- `address` (type String)

**Returns:** type void

### getOscModule()

Gets the OpenSoundControl module.

**Returns:** type OscModule

### allocateMemoryBlock(int)

Allocates some memory that will be automatically freed once the extension exits.

**Parameters:**
- `size` (type int)

**Returns:** type MemoryBlock

### createBitmap(int,int,com.bitwig.extension.api.graphics.BitmapFormat)

Creates an offscreen bitmap that the extension can use to render into. The memory used by this bitmap is
 guaranteed to be freed once this extension exits.

**Parameters:**
- `width` (type int)
- `height` (type int)
- `format` (type BitmapFormat)

**Returns:** type Bitmap

### loadFontFace(java.lang.String)

Loads a font.
 The memory used by this font is guaranteed to be freed once this extension exits.

**Parameters:**
- `path` (type String)

**Returns:** type FontFace

### createFontOptions()

Creates a new FontOptions.
 This object is used to configure how the GraphicOutput will display text.
 The memory used by this object is guaranteed to be freed once this extension exits.

**Returns:** type FontOptions

### loadPNG(java.lang.String)

Loads a PNG image.
 The memory used by this image is guaranteed to be freed once this extension exits.

**Parameters:**
- `path` (type String)

**Returns:** type Image

### loadSVG(java.lang.String,double)

Loads a SVG image.
 The memory used by this image is guaranteed to be freed once this extension exits.

**Parameters:**
- `path` (type String)
- `scale` (type double)

**Returns:** type Image

