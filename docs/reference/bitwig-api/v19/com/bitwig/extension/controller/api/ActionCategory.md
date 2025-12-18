# ActionCategory

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface are used to categorize actions in Bitwig Studio. The list of action categories
 provided by Bitwig Studio can be queried by calling Application.getActionCategories(). To receive a
 specific action category call Application.getActionCategory(String).

## Methods

### getId()

Returns a string the identifies this action category uniquely.

**Returns:** type String - the identifier string

### getName()

Returns the name of this action category.

**Returns:** type String - the name string

### getActions()

Lists all actions in this category.

**Returns:** type Action[] - the array of actions in this category

