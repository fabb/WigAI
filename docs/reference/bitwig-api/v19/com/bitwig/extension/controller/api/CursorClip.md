# CursorClip

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Represents a cursor clip.

**Extends:** `com.bitwig.extension.controller.api.Clip`, `com.bitwig.extension.controller.api.Cursor`

## Methods

### selectClip(com.bitwig.extension.controller.api.Clip)

Requests that the supplied clip be selected in this cursor.

**Parameters:**
- `clip` (type Clip)

**Returns:** type void

### scrollToKey(int)

Scroll the note grid so that the given key becomes the key with y position of 0.

 Note: This can cause some parts of the grid to represent invalid keys as there is no clipping

**Parameters:**
- `key` (type int): the key that should be the new key with a y position of 0. This must be a value in the range
           0...127.

**Returns:** type void

### scrollKeysPageUp()

Scrolls the note grid keys one page up. For example if the note grid is configured to show 12 keys and
 is currently showing keys [36..47], calling this method would scroll the note grid to key range
 [48..59].

**Returns:** type void

### scrollKeysPageDown()

Scrolls the note grid keys one page down. For example if the note grid is configured to show 12 keys and
 is currently showing keys [36..47], calling this method would scroll the note grid to key range
 [48..59].

**Returns:** type void

### scrollKeysStepUp()

Scrolls the note grid keys one key up. For example if the note grid is configured to show 12 keys and is
 currently showing keys [36..47], calling this method would scroll the note grid to key range [37..48].

**Returns:** type void

### scrollKeysStepDown()

Scrolls the note grid keys one key down. For example if the note grid is configured to show 12 keys and
 is currently showing keys [36..47], calling this method would scroll the note grid to key range
 [35..46].

**Returns:** type void

### scrollToStep(int)

Scroll the note grid so that the given step becomes visible.

**Parameters:**
- `step` (type int): the step that should become visible

**Returns:** type void

### scrollStepsPageForward()

Scrolls the note grid steps one page forward. For example if the note grid is configured to show 16
 steps and is currently showing keys [0..15], calling this method would scroll the note grid to key range
 [16..31].

**Returns:** type void

### scrollStepsPageBackwards()

Scrolls the note grid steps one page backwards. For example if the note grid is configured to show 16
 steps and is currently showing keys [16..31], calling this method would scroll the note grid to key
 range [0..16].

**Returns:** type void

### scrollStepsStepForward()

Scrolls the note grid steps one step forward. For example if the note grid is configured to show 16
 steps and is currently showing keys [0..15], calling this method would scroll the note grid to key range
 [1..16].

**Returns:** type void

### scrollStepsStepBackwards()

Scrolls the note grid steps one step backwards. For example if the note grid is configured to show 16
 steps and is currently showing keys [1..16], calling this method would scroll the note grid to key range
 [0..15].

**Returns:** type void

### canScrollKeysUp()

Value that reports if the note grid keys can be scrolled further up.

**Returns:** type BooleanValue

### addCanScrollKeysUpObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the note grid keys can be scrolled further up.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### canScrollKeysDown()

Value that reports if the note grid keys can be scrolled further down.

**Returns:** type BooleanValue

### addCanScrollKeysDownObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the note grid keys can be scrolled further down.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### canScrollStepsBackwards()

Value that reports if the note grid if the note grid steps can be scrolled backwards.

**Returns:** type BooleanValue

### addCanScrollStepsBackwardsObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the note grid steps can be scrolled backwards.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### canScrollStepsForwards()

Value that reports if the note grid if the note grid steps can be scrolled forwards.

**Returns:** type BooleanValue

### addCanScrollStepsForwardObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers an observer that reports if the note grid keys can be scrolled forward.

**Parameters:**
- `callback` (type BooleanValueChangedCallback): a callback function that receives a single boolean parameter.

**Returns:** type void

### toggleStep(int,int,int)

**Parameters:**
- `x` (type int)
- `y` (type int)
- `insertVelocity` (type int)

**Returns:** type void

### toggleStep(int,int,int,int)

Toggles the existence of a note in the note grid cell specified by the given x and y arguments.

**Parameters:**
- `channel` (type int): the MIDI channel, between 0 and 15.
- `x` (type int): the x position within the note grid, defining the step/time of the target note
- `y` (type int): the y position within the note grid, defining the key of the target note
- `insertVelocity` (type int): the velocity of the target note in case a new note gets inserted

**Returns:** type void

### setStep(int,int,int,double)

**Parameters:**
- `x` (type int)
- `y` (type int)
- `insertVelocity` (type int)
- `insertDuration` (type double)

**Returns:** type void

### setStep(int,int,int,int,double)

Creates a note in the grid cell specified by the given x and y arguments. Existing notes are
 overwritten.

**Parameters:**
- `channel` (type int)
- `x` (type int): the x position within the note grid, defining the step/time of the new note
- `y` (type int): the y position within the note grid, defining the key of the new note
- `insertVelocity` (type int): the velocity of the new note
- `insertDuration` (type double): the duration of the new note

**Returns:** type void

### clearStep(int,int)

**Parameters:**
- `x` (type int)
- `y` (type int)

**Returns:** type void

### clearStep(int,int,int)

Removes the note in the grid cell specified by the given x and y arguments. Calling this method does
 nothing in case no note exists at the given x-y-coordinates.

**Parameters:**
- `channel` (type int): MIDI channel, from 0 to 15.
- `x` (type int): the x position within the note grid, defining the step/time of the target note
- `y` (type int): the y position within the note grid, defining the key of the target note

**Returns:** type void

### clearStepsAtX(int,int)

Removes all notes in the grid started on the step x.

**Parameters:**
- `channel` (type int)
- `x` (type int)

**Returns:** type void

### clearSteps(int)

**Parameters:**
- `y` (type int)

**Returns:** type void

### clearStepsAtY(int,int)

Removes all notes in the grid row specified by the given y argument.

**Parameters:**
- `channel` (type int): MIDI channel, from 0 to 15.
- `y` (type int): the y position within the note grid, defining the key of the target note

**Returns:** type void

### clearSteps()

Removes all notes in the grid.

**Returns:** type void

### moveStep(int,int,int,int)

**Parameters:**
- `x` (type int)
- `y` (type int)
- `dx` (type int)
- `dy` (type int)

**Returns:** type void

### moveStep(int,int,int,int,int)

Moves a note in the note grid cell specified by the given x and y arguments to the grid cell (x + dx, y + dy).

**Parameters:**
- `channel` (type int): MIDI channel, from 0 to 15.
- `x` (type int): the x position within the note grid, defining the step/time of the target note
- `y` (type int): the y position within the note grid, defining the key of the target note
- `dx` (type int): the offset in x direction
- `dy` (type int): the offset in y direction

**Returns:** type void

### selectStepContents(int,int,boolean)

**Parameters:**
- `x` (type int)
- `y` (type int)
- `clearCurrentSelection` (type boolean)

**Returns:** type void

### selectStepContents(int,int,int,boolean)

Selects the note in the grid cell specified by the given x and y arguments, in case there actually is a
 note at the given x-y-coordinates.

**Parameters:**
- `channel` (type int): MIDI channel, from 0 to 15.
- `x` (type int): the x position within the note grid, defining the step/time of the target note
- `y` (type int): the y position within the note grid, defining the key of the target note
- `clearCurrentSelection` (type boolean): `true` if the existing selection should be cleared, false if the note should be added to
           the current selection.

**Returns:** type void

### setStepSize(double)

Sets the beat time duration that is represented by one note grid step.

**Parameters:**
- `lengthInBeatTime` (type double): the length of one note grid step in beat time.

**Returns:** type void

### addStepDataObserver(com.bitwig.extension.callback.StepDataChangedCallback)

Registers an observer that reports which note grid steps/keys contain notes.

**Parameters:**
- `callback` (type StepDataChangedCallback): A callback function that receives three parameters: 1. the x (step) coordinate within the note
           grid (integer), 2. the y (key) coordinate within the note grid (integer), and 3. an integer
           value that indicates if the step is empty (`0`) or if a note continues playing (`1`) or starts
           playing (`2`).

**Returns:** type void

### addNoteStepObserver(com.bitwig.extension.callback.NoteStepChangedCallback)

Registers an observer that reports which note grid steps/keys contain notes.

**Parameters:**
- `callback` (type NoteStepChangedCallback): A callback function that receives the StepInfo.

**Returns:** type void

### playingStep()

Value that reports note grid cells as they get played by the sequencer.

**Returns:** type IntegerValue

### addPlayingStepObserver(com.bitwig.extension.callback.IntegerValueChangedCallback)

Registers an observer that reports note grid cells as they get played by the sequencer.

**Parameters:**
- `callback` (type IntegerValueChangedCallback): A callback function that receives a single integer parameter, which reflects the step
           coordinate that is played, or -1 if no step is associated with the current playback position.

**Returns:** type void

### setName(java.lang.String)

Updates the name of the clip.

**Parameters:**
- `name` (type String): the new clip name

**Returns:** type void

### getShuffle()

Returns shuffle settings of the clip.

**Returns:** type SettableBooleanValue - the value object that represents the clips shuffle setting.

### getAccent()

Returns accent setting of the clip.

**Returns:** type SettableRangedValue - the ranged value object that represents the clips accent setting.

### getPlayStart()

Returns the start of the clip in beat time.

**Returns:** type SettableBeatTimeValue - the beat time object that represents the clips start time.

### getPlayStop()

Returns the length of the clip in beat time.

**Returns:** type SettableBeatTimeValue - the beat time object that represents the duration of the clip.

### isLoopEnabled()

Returns an object that provides access to the loop enabled state of the clip.

**Returns:** type SettableBooleanValue - a boolean value object.

### getLoopStart()

Returns the loop start time of the clip in beat time.

**Returns:** type SettableBeatTimeValue - the beat time object that represents the clips loop start time.

### getLoopLength()

Returns the loop length of the clip in beat time.

**Returns:** type SettableBeatTimeValue - the beat time object that represents the clips loop length.

### addColorObserver(com.bitwig.extension.callback.ColorValueChangedCallback)

Registers an observer that reports the clip color.

**Parameters:**
- `callback` (type ColorValueChangedCallback): a callback function that receives three parameters: 1. the red coordinate of the RBG color
           value, 2. the green coordinate of the RBG color value, and 3. the blue coordinate of the RBG
           color value

**Returns:** type void

### color()

Get the color of the clip.

**Returns:** type SettableColorValue

### duplicate()

Duplicates the clip.

**Returns:** type void

### duplicateContent()

Duplicates the content of the clip.

**Returns:** type void

### transpose(int)

Transposes all notes in the clip by the given number of semitones.

**Parameters:**
- `semitones` (type int): the amount of semitones to transpose, can be a positive or negative integer value.

**Returns:** type void

### quantize(double)

Quantize the start time of all notes in the clip according to the given amount. The note lengths remain
 the same as before.

**Parameters:**
- `amount` (type double): a factor between `0` and `1` that allows to morph between the original note start and the
           quantized note start.

**Returns:** type void

### getTrack()

Gets the track that contains the clip.

**Returns:** type Track - a track object that represents the track which contains the clip.

### launchQuantization()

Setting for the default launch quantization.

 Possible values are "default", "none", "8", "4", "2", "1", "1/2", "1/4", "1/8", "1/16"

**Returns:** type SettableEnumValue

### useLoopStartAsQuantizationReference()

Setting "Q to loop" in the inspector.

**Returns:** type SettableBooleanValue

### launchLegato()

Setting "Legato" from the inspector.

**Returns:** type SettableBooleanValue

### launchMode()

Setting "Launch Mode" from the inspector.
 Possible values are:
  - default
  - from_start
  - continue_or_from_start
  - continue_or_synced
  - synced

**Returns:** type SettableEnumValue

### getStep(int,int,int)

Get step info

**Parameters:**
- `channel` (type int)
- `x` (type int)
- `y` (type int)

**Returns:** type NoteStep

### launch()

Launches the clip.

**Returns:** type void

### launchWithOptions(java.lang.String,java.lang.String)

Launches with the given options:

**Parameters:**
- `quantization` (type String): possible values are "default", "none", "8", "4", "2", "1", "1/2", "1/4", "1/8", "1/16"
- `launchMode` (type String): possible values are: "default", "from_start", "continue_or_from_start",
                   "continue_or_synced", "synced"

**Returns:** type void

### clipLauncherSlot()

Get the clip launcher slot containing the clip.

**Returns:** type ClipLauncherSlot

### showInEditor()

Open the detail editor and show the clip.

**Returns:** type void

### exists()

Returns a value object that indicates if the object being proxied exists, or if it has content.

**Returns:** type BooleanValue

### createEqualsValue(com.bitwig.extension.controller.api.ObjectProxy)

Creates a BooleanValue that determines this proxy is considered equal to another proxy. For this
 to be the case both proxies need to be proxying the same target object.

**Parameters:**
- `other` (type ObjectProxy)

**Returns:** type BooleanValue

### isSubscribed()

Determines if this object is currently 'subscribed'. In the subscribed state it will notify any
 observers registered on it.

**Returns:** type boolean

### setIsSubscribed(boolean)

Sets whether the driver currently considers this object 'active' or not.

**Parameters:**
- `value` (type boolean)

**Returns:** type void

### subscribe()

Subscribes the driver to this object.

**Returns:** type void

### unsubscribe()

Unsubscribes the driver from this object.

**Returns:** type void

### selectPrevious()

Select the previous item.

**Returns:** type void

### selectPreviousAction()

**Returns:** type HardwareActionBindable

### selectNext()

Select the next item.

**Returns:** type void

### selectNextAction()

**Returns:** type HardwareActionBindable

### selectFirst()

Select the first item.

**Returns:** type void

### selectLast()

Select the last item.

**Returns:** type void

### hasNext()

Boolean value that reports whether there is an item after the current cursor position.

**Returns:** type BooleanValue

### hasPrevious()

Boolean value that reports whether there is an item before the current cursor position.

**Returns:** type BooleanValue

### addCanSelectPreviousObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers a function with bool argument that gets called when the previous item gains or remains
 selectable.

**Parameters:**
- `callback` (type BooleanValueChangedCallback)

**Returns:** type void

### addCanSelectNextObserver(com.bitwig.extension.callback.BooleanValueChangedCallback)

Registers a function with bool argument that gets called when the next item gains or remains
 selectable.

**Parameters:**
- `callback` (type BooleanValueChangedCallback)

**Returns:** type void

### addBinding(com.bitwig.extension.controller.api.RelativeHardwareControl)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)

**Returns:** type RelativeHardwareControlBinding

### addBindingWithSensitivity(com.bitwig.extension.controller.api.RelativeHardwareControl,double)

Binds this target to the supplied hardware control so that when the user moves the hardware control this
 target will respond in a meaningful way.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `hardwareControl` (type RelativeHardwareControl)
- `sensitivity` (type double)

**Returns:** type RelativeHardwareControlBinding

