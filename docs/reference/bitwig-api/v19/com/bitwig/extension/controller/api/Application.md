# Application

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

An interface that provides methods for accessing the most common global application commands.

 In addition, functions are provided for accessing any application action in a generic and categorized way,
 pretty much as displayed in the Bitwig Studio commander dialog (see getActions(),
 getAction(String), getActionCategories()), getActionCategory(String)).

 To receive an instance of the application interface call ControllerHost.createApplication().

## Methods

### createAudioTrack(int)

Creates a new audio track at the given position.

**Parameters:**
- `position` (type int): the index within the list of main tracks where the new track should be inserted, or `-1` in
           case the track should be inserted at the end of the list. Values outside the valid range will
           get pinned to the valid range, so the actual position might be different from the provided
           parameter value.

**Returns:** type void

### createInstrumentTrack(int)

Creates a new instrument track at the given position.

**Parameters:**
- `position` (type int): the index within the list of main tracks where the new track should be inserted, or `-1` in
           case the track should be inserted at the end of the list. Values outside the valid range will
           get pinned to the valid range, so the actual position might be different from the provided
           parameter value.

**Returns:** type void

### createEffectTrack(int)

Creates a new effect track at the given position.

**Parameters:**
- `position` (type int): the index within the list of effect tracks where the new track should be inserted, or `-1` in
           case the track should be inserted at the end of the list. Values outside the valid range will
           get pinned to the valid range, so the actual position might be different from the provided
           parameter value.

**Returns:** type void

### getActions()

Returns a list of actions that the application supports. Actions are commands in Bitwig Studio that are
 typically accessible through menus or keyboard shortcuts.

 Please note that many of the commands encapsulated by the reported actions are also accessible through
 other (probably more convenient) interfaces methods of the API. In contrast to that, this method
 provides a more generic way to find available application functionality.

**Returns:** type Action[] - the list of actions

### getAction(java.lang.String)

Returns the action for the given action identifier. For a list of available actions, see
 getActions().

**Parameters:**
- `id` (type String): the action identifier string, must not be `null`

**Returns:** type Action - the action associated with the given id, or null in case there is no action with the given
         identifier.

### getActionCategories()

Returns a list of action categories that is used by Bitwig Studio to group actions into categories.

**Returns:** type ActionCategory[] - the list of action categories

### getActionCategory(java.lang.String)

Returns the action category associated with the given identifier. For a list of available action
 categories, see getActionCategories().

**Parameters:**
- `id` (type String): the category identifier string, must not be `null`

**Returns:** type ActionCategory - the action associated with the given id, or null in case there is no category with the given
         identifier

### activateEngine()

Activates the audio engine in Bitwig Studio.

**Returns:** type void

### deactivateEngine()

Deactivates the audio engine in Bitwig Studio.

**Returns:** type void

### hasActiveEngine()

Value that reports whether an audio engine is active or not.

**Returns:** type BooleanValue

### addHasActiveEngineObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that gets called when the audio engine becomes active or inactive.

**Parameters:**
- `callable` (type BooleanValueChangedCallback): a callback function that accepts a single boolean parameter. The callback parameter indicates
           whether the audio engine became active (true) or inactive (false).

**Returns:** type void

### projectName()

Value that reports the name of the current project.

**Returns:** type StringValue

### addProjectNameObserver(com.bitwig.extension.callback.StringValueChangedCallback,int)

Registers an observer that reports the name of the current project.

**Parameters:**
- `callback` (type StringValueChangedCallback): a callback function that accepts a single string parameter.
- `maxChars` (type int): the maximum length of the reported name. Longer names will get truncated.

**Returns:** type void

### nextProject()

Switches to the next project tab in Bitwig Studio.

**Returns:** type void

### previousProject()

Switches to the previous project tab in Bitwig Studio.

**Returns:** type void

### navigateIntoTrackGroup(com.bitwig.extension.controller.api.Track)

Set BitwigStudio to navigate into the group.

**Parameters:**
- `track` (type Track)

**Returns:** type void

### navigateToParentTrackGroup()

Set BitwigStudio to navigate into the parent group.

**Returns:** type void

### undo()

Sends an undo command to Bitwig Studio.

**Returns:** type void

### undoAction()

**Returns:** type HardwareActionBindable

### canUndo()

Value that reports if there is an action to undo.

**Returns:** type BooleanValue

### redo()

Sends a redo command to Bitwig Studio.

**Returns:** type void

### redoAction()

**Returns:** type HardwareActionBindable

### canRedo()

Value that reports if there is an action to redo.

**Returns:** type BooleanValue

### setPanelLayout(java.lang.String)

Switches the Bitwig Studio user interface to the panel layout with the given name. The list of available
 panel layouts depends on the active display profile.

**Parameters:**
- `panelLayout` (type String): the name of the new panel layout

**Returns:** type void

### nextPanelLayout()

Switches to the next panel layout of the active display profile in Bitwig Studio.

**Returns:** type void

### previousPanelLayout()

Switches to the previous panel layout of the active display profile in Bitwig Studio.

**Returns:** type void

### panelLayout()

Value that reports the name of the active panel layout.

**Returns:** type StringValue

### addPanelLayoutObserver(com.bitwig.extension.callback.StringValueChangedCallback,int)

Registers an observer that reports the name of the active panel layout.

**Parameters:**
- `callable` (type StringValueChangedCallback): a callback function object that accepts a single string parameter
- `maxChars` (type int): the maximum length of the panel layout name

**Returns:** type void

### displayProfile()

Value that reports the name of the active display profile.

**Returns:** type StringValue

### addDisplayProfileObserver(com.bitwig.extension.callback.StringValueChangedCallback,int)

Registers an observer that reports the name of the active display profile.

**Parameters:**
- `callable` (type StringValueChangedCallback): a callback function object that accepts a single string parameter
- `maxChars` (type int): the maximum length of the display profile name

**Returns:** type void

### toggleInspector()

Toggles the visibility of the inspector panel.

**Returns:** type void

### toggleDevices()

Toggles the visibility of the device chain panel.

**Returns:** type void

### toggleMixer()

Toggles the visibility of the mixer panel.

**Returns:** type void

### toggleNoteEditor()

Toggles the visibility of the note editor panel.

**Returns:** type void

### toggleAutomationEditor()

Toggles the visibility of the automation editor panel.

**Returns:** type void

### toggleBrowserVisibility()

Toggles the visibility of the browser panel.

**Returns:** type void

### previousSubPanel()

Shows the previous detail panel (note editor, device, automation).

**Returns:** type void

### nextSubPanel()

Shows the next detail panel (note editor, device, automation).

**Returns:** type void

### arrowKeyLeft()

Equivalent to an Arrow-Left key stroke on the computer keyboard. The concrete functionality depends on
 the current keyboard focus in Bitwig Studio.

**Returns:** type void

### arrowKeyRight()

Equivalent to an Arrow-Right key stroke on the computer keyboard. The concrete functionality depends on
 the current keyboard focus in Bitwig Studio.

**Returns:** type void

### arrowKeyUp()

Equivalent to an Arrow-Up key stroke on the computer keyboard. The concrete functionality depends on the
 current keyboard focus in Bitwig Studio.

**Returns:** type void

### arrowKeyDown()

Equivalent to an Arrow-Down key stroke on the computer keyboard. The concrete functionality depends on
 the current keyboard focus in Bitwig Studio.

**Returns:** type void

### enter()

Equivalent to an Enter key stroke on the computer keyboard. The concrete functionality depends on the
 current keyboard focus in Bitwig Studio.

**Returns:** type void

### escape()

Equivalent to an Escape key stroke on the computer keyboard. The concrete functionality depends on the
 current keyboard focus in Bitwig Studio.

**Returns:** type void

### selectAll()

Selects all items according the current selection focus in Bitwig Studio.

**Returns:** type void

### selectAllAction()

**Returns:** type HardwareActionBindable

### selectNone()

Deselects any items according the current selection focus in Bitwig Studio.

**Returns:** type void

### selectNoneAction()

**Returns:** type HardwareActionBindable

### selectPrevious()

Selects the previous item in the current selection.

**Returns:** type void

### selectPreviousAction()

**Returns:** type HardwareActionBindable

### selectNext()

Selects the next item in the current selection.

**Returns:** type void

### selectNextAction()

**Returns:** type HardwareActionBindable

### selectFirst()

Selects the first item in the current selection.

**Returns:** type void

### selectFirstAction()

**Returns:** type HardwareActionBindable

### selectLast()

Selects the last item in the current selection.

**Returns:** type void

### selectLastAction()

**Returns:** type HardwareActionBindable

### cut()

Cuts the selected items in Bitwig Studio if applicable.

**Returns:** type void

### cutAction()

**Returns:** type HardwareActionBindable

### copy()

Copies the selected items in Bitwig Studio to the clipboard if applicable.

**Returns:** type void

### copyAction()

**Returns:** type HardwareActionBindable

### paste()

Pastes the clipboard contents into the current selection focus in Bitwig Studio if applicable.

**Returns:** type void

### pasteAction()

**Returns:** type HardwareActionBindable

### duplicate()

Duplicates the active selection in Bitwig Studio if applicable.

**Returns:** type void

### duplicateAction()

**Returns:** type HardwareActionBindable

### remove()

Deletes the selected items in Bitwig Studio if applicable. Originally this function was called `delete`
 (Bitwig Studio 1.0). But as `delete` is reserved in JavaScript this function got renamed to `remove` in
 Bitwig Studio 1.0.9.

**Returns:** type void

### removeAction()

**Returns:** type HardwareActionBindable

### rename()

Opens a text input field in Bitwig Studio for renaming the selected item.

**Returns:** type void

### zoomIn()

Zooms in one step into the currently focused editor of the Bitwig Studio user interface.

**Returns:** type void

### zoomInAction()

**Returns:** type HardwareActionBindable

### zoomOut()

Zooms out one step in the currently focused editor of the Bitwig Studio user interface.

**Returns:** type void

### zoomOutAction()

**Returns:** type HardwareActionBindable

### zoomLevel()

Same as zoomIn/zoomOut, but as a stepper

**Returns:** type RelativeHardwarControlBindable

### zoomToSelection()

Adjusts the zoom level of the currently focused editor so that it matches the active selection.

**Returns:** type void

### zoomToSelectionAction()

**Returns:** type HardwareActionBindable

### zoomToSelectionOrAll()

Toggles between zoomToSelection and zoomToFit.

**Returns:** type void

### zoomToSelectionOrAllAction()

**Returns:** type HardwareActionBindable

### zoomToSelectionOrPrevious()

Toggles between zoomToSelection and the last śet zoom level.

**Returns:** type void

### zoomToSelectionOrPreviousAction()

**Returns:** type HardwareActionBindable

### zoomToFit()

Adjusts the zoom level of the currently focused editor so that all content becomes visible.

**Returns:** type void

### zoomToFitAction()

**Returns:** type HardwareActionBindable

### focusPanelToLeft()

Moves the panel focus to the panel on the left of the currently focused panel.

**Returns:** type void

### focusPanelToRight()

Moves the panel focus to the panel right to the currently focused panel.

**Returns:** type void

### focusPanelAbove()

Moves the panel focus to the panel above the currently focused panel.

**Returns:** type void

### focusPanelBelow()

Moves the panel focus to the panel below the currently focused panel.

**Returns:** type void

### toggleFullScreen()

Toggles between full screen and windowed user interface.

**Returns:** type void

### setPerspective(java.lang.String)

**Parameters:**
- `perspective` (type String)

**Returns:** type void

### nextPerspective()

**Returns:** type void

### previousPerspective()

**Returns:** type void

### addSelectedModeObserver(com.bitwig.extension.callback.StringValueChangedCallback,int,java.lang.String)

**Parameters:**
- `callable` (type StringValueChangedCallback)
- `maxChars` (type int)
- `fallbackText` (type String)

**Returns:** type void

### recordQuantizationGrid()

Returns the record quantization grid setting from the preferences.
 Possible values are "OFF", "1/32", "1/16", "1/8", "1/4".

**Returns:** type SettableEnumValue

### recordQuantizeNoteLength()

Returns a settable value to choose if the record quantization should quantize note length.

**Returns:** type SettableBooleanValue

