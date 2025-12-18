# HardwareAction

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

An action that can happen on a hardware control. For example, the user touching it, pressing it, releasing
 it etc.

**Extends:** `com.bitwig.extension.controller.api.HardwareBindingSource`

## Methods

### setActionMatcher(com.bitwig.extension.controller.api.HardwareActionMatcher)

Sets the HardwareActionMatcher that is used to recognize when this action happens.

**Parameters:**
- `actionMatcher` (type HardwareActionMatcher)

**Returns:** type void

### setPressureActionMatcher(com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher)

Sets the AbsoluteHardwareValueMatcher that is used to recognize when this action happens and
 with what pressure.

 This is useful for a button press that is pressure sensitive. The pressure can be obtained by creating a
 custom action with
 ControllerHost.createAction(java.util.function.DoubleConsumer, java.util.function.Supplier) and
 then binding the created action to this HardwareAction.

**Parameters:**
- `actionMatcher` (type AbsoluteHardwareValueMatcher)

**Returns:** type void

### isSupported()

Checks if this action is supported (that is, it has a HardwareActionMatcher that can detect it).

**Returns:** type boolean

### setShouldFireEvenWhenUsedAsNoteInput(boolean)

Decides if this action should fire even if the hardware input that matched it was also used as note
 input. Note input is defined as input that matches a NoteInput mask and its event translations.
 Usually events should only be note input or actions but not both at the same time (this is the default
 state). However, occasionally it is useful for a note event to be played as both note input and also
 trigger some action. For example, a drum pad may play a note but in a special mode on the controller it
 should also select the pad somehow. In this case it would be both note input and the action that fires
 to select the pad.

**Parameters:**
- `value` (type boolean)

**Returns:** type void

### canBindTo(java.lang.Object)

Checks if it is possible to make a binding from this source to the supplied target object.

**Parameters:**
- `target` (type Object)

**Returns:** type boolean

### addBinding(com.bitwig.extension.controller.api.HardwareBindable)

Binds this source to the supplied target and returns the created binding. This can only be called if the
 canBindTo(Object) returns true.

**Parameters:**
- `target` (type HardwareBindable)

**Returns:** type HardwareBindingType

### clearBindings()

Clears all bindings from this source to its targets.

**Returns:** type void

### setBinding(com.bitwig.extension.controller.api.HardwareBindable)

Ensures there is a single binding to the supplied target.

 This is a convenience method that is equivalent to calling clearBindings() and the
 addBinding(HardwareBindable)

**Parameters:**
- `target` (type HardwareBindable)

**Returns:** type HardwareBindingType

