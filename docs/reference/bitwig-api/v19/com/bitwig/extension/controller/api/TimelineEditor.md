# TimelineEditor

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Shared functions between `Arranger` and `DetailEditor`

## Methods

### zoomInAction()

Zooms in the timeline, if the timeline editor is visible.

**Returns:** type HardwareActionBindable

### zoomIn()

**Returns:** type void

### zoomOutAction()

Zooms out the timeline, if the timeline editor is visible.

**Returns:** type HardwareActionBindable

### zoomOut()

**Returns:** type void

### zoomLevel()

Smoothly adjusts the zoom level

**Returns:** type RelativeHardwarControlBindable

### zoomToFitAction()

Adjusts the zoom level of the timeline so that all content becomes visible, if the timeline editor is visible.

**Returns:** type HardwareActionBindable

### zoomToFit()

**Returns:** type void

### zoomToSelectionAction()

Adjusts the zoom level of the timeline so that it matches the active selection, if the timeline editor is visible.

**Returns:** type HardwareActionBindable

### zoomToSelection()

**Returns:** type void

### zoomToFitSelectionOrAllAction()

Toggles the timeline between zoomToSelection and zoomToFit, if it is visible.

**Returns:** type HardwareActionBindable

### zoomToFitSelectionOrAll()

**Returns:** type void

### zoomToFitSelectionOrPreviousAction()

Toggles the timeline between zoomToSelection and the last śet zoom level, if it is visible.

**Returns:** type HardwareActionBindable

### zoomToFitSelectionOrPrevious()

**Returns:** type void

### getHorizontalScrollbarModel()

Get the horizontal (time) scrollbar model.

**Returns:** type ScrollbarModel

