# Action

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent actions in Bitwig Studio, such as commands that can be launched from
 the main menu or via keyboard shortcuts.

 To receive the list of all actions provided by Bitwig Studio call Application.getActions(). The
 list of actions that belong to a certain category can be queried by calling
 ActionCategory.getActions(). Access to specific actions is provided in
 Application.getAction(String).

**Extends:** `com.bitwig.extension.controller.api.HardwareActionBindable`

## Methods

### getId()

Returns a string the identifies this action uniquely.

**Returns:** type String - the identifier string

### getName()

Returns the name of this action.

**Returns:** type String - the name string

### getCategory()

Returns the category of this action.

**Returns:** type ActionCategory - the category string

### getMenuItemText()

Returns the text that is displayed in menu items associated with this action.

**Returns:** type String - the menu item text

### invoke()

Invokes the action.

**Returns:** type void

### addBinding(com.bitwig.extension.controller.api.HardwareAction)

Binds this target to the supplied HardwareAction so that when the hardware action occurs this
 target is invoked.

 When the binding is no longer needed the HardwareBinding.removeBinding() method can be called on
 it.

**Parameters:**
- `action` (type HardwareAction)

**Returns:** type HardwareActionBinding

