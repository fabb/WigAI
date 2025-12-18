# Setting

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

A common base interface for labeled and categorized settings.

## Methods

### getCategory()

Returns the category name of the setting.

**Returns:** type String - a string value containing the category name

### getLabel()

Returns the label text of the setting.

**Returns:** type String - a string value containing the label text

### enable()

Marks the settings as enabled in Bitwig Studio. By default the setting is enabled.

**Returns:** type void

### disable()

Marks the settings as disabled in Bitwig Studio. By default the setting is enabled.

**Returns:** type void

### show()

Shows the setting in Bitwig Studio. By default the setting is shown.

**Returns:** type void

### hide()

Hides the setting in Bitwig Studio. By default the setting is shown.

**Returns:** type void

