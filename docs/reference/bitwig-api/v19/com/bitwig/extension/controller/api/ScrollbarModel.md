# ScrollbarModel

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Interface providing detailed access to a specific scrollbar.

## Methods

### isZoomable()

Does this ScrollbarModel support zoom?.

**Returns:** type boolean

### getContentPerPixel()

Get the zoom level expressed as content units per pixel.

**Returns:** type DoubleValue

### zoomAtPosition(double,double)

Zoom in/out around a specific position (in content units). The distance is given in 2ˣ, so +1 implies 200% of the
 current level and -1 implies 50%

**Parameters:**
- `position` (type double)
- `distance` (type double)

**Returns:** type void

### zoomToFit()

Adjusts the zoom level so it fits all content

**Returns:** type void

### zoomToSelection()

Adjusts the zoom level so it fits the selected content

**Returns:** type void

### zoomToFitSelectionOrAll()

Alternate the zoom level between fitting all content or the selection

**Returns:** type void

### zoomToFitSelectionOrCurrent()

Alternate the zoom level between fitting the selected content or the previous zoom level

**Returns:** type void

### zoomToContentRegion(double,double)

Set the zoom level to fit a specific content range.

**Parameters:**
- `from` (type double)
- `to` (type double)

**Returns:** type void

