# UserControlBank

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent a bank of custom controls that can be manually learned to device
 parameters by the user.

## Methods

### getControl(int)

Gets the user control at the given bank index.

**Parameters:**
- `index` (type int): the index of the control within the bank

**Returns:** type Parameter - the requested user control object

