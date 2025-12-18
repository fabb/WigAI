# Bitmap

- Kind: interface
- Package: `com.bitwig.extension.api.graphics`

Represents a bitmap image which can be painted via render(Renderer).

**Extends:** `com.bitwig.extension.api.graphics.Image`

## Methods

### getWidth()

Returns the width

**Returns:** type int

### getHeight()

Returns the height

**Returns:** type int

### getFormat()

**Returns:** type BitmapFormat

### getMemoryBlock()

**Returns:** type MemoryBlock

### render(com.bitwig.extension.api.graphics.Renderer)

Call this method to start painting the bitmap.
 This method will take care of disposing allocated patterns during the rendering.

**Parameters:**
- `renderer` (type Renderer)

**Returns:** type void

### showDisplayWindow()

Call this method to show a window which displays the bitmap.
 You should see this as a debug utility rather than a Control Surface API feature.

**Returns:** type void

### setDisplayWindowTitle(java.lang.String)

Updates the display window title.

**Parameters:**
- `title` (type String)

**Returns:** type void

### saveToDiskAsPPM(java.lang.String)

Saves the image as a PPM file.

**Parameters:**
- `path` (type String): the location of the target file.

**Returns:** type void

