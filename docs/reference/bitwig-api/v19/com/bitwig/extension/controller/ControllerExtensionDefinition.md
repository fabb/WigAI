# ControllerExtensionDefinition

- Kind: abstract class
- Package: `com.bitwig.extension.controller`

Defines an extension that enabled a controller to work with Bitwig Studio.

**Extends:** com.bitwig.extension.ExtensionDefinition

## Methods

### toString()

**Returns:** type String

### getHardwareVendor()

The vendor of the controller that this extension is for.

**Returns:** type String

### getHardwareModel()

The model name of the controller that this extension is for.

**Returns:** type String

### getNumMidiInPorts()

The number of MIDI in ports that this controller extension has.

**Returns:** type int

### getNumMidiOutPorts()

The number of MIDI out ports that this controller extension has.

**Returns:** type int

### getAutoDetectionMidiPortNamesList(com.bitwig.extension.api.PlatformType)

Obtains a AutoDetectionMidiPortNamesList that defines the names of the MIDI in and out ports
 that can be used for auto detection of the controller for the supplied platform type.

**Parameters:**
- `platformType` (type PlatformType)

**Returns:** type AutoDetectionMidiPortNamesList

### listAutoDetectionMidiPortNames(com.bitwig.extension.controller.AutoDetectionMidiPortNamesList,com.bitwig.extension.api.PlatformType)

Lists the AutoDetectionMidiPortNames that defines the names of the MIDI in and out ports that
 can be used for auto detection of the controller for the supplied platform type.

**Parameters:**
- `list` (type AutoDetectionMidiPortNamesList)
- `platformType` (type PlatformType)

**Returns:** type void

### getHardwareDeviceMatcherList()

**Returns:** type HardwareDeviceMatcherList

### listHardwareDevices(com.bitwig.extension.controller.HardwareDeviceMatcherList)

Lists the hardware devices that this controller needs to function. For each device that is listed the
 user will see a chooser in the preferences for this extension that allows them to choose a connected
 device. The HardwareDeviceMatcher will also be used during auto detection to automatically add
 and select the device if possible.

**Parameters:**
- `list` (type HardwareDeviceMatcherList)

**Returns:** type void

### createInstance(com.bitwig.extension.controller.api.ControllerHost)

Creates an instance of this extension.

**Parameters:**
- `host` (type ControllerHost)

**Returns:** type ControllerExtension

### getName()

The name of the extension.

**Returns:** type String

### getAuthor()

The author of the extension.

**Returns:** type String

### getVersion()

The version of the extension.

**Returns:** type String

### getId()

A unique id that identifies this extension.

**Returns:** type UUID

### getRequiredAPIVersion()

The minimum API version number that this extensions requires.

**Returns:** type int

### isUsingBetaAPI()

Is this extension is using Beta APIs?

 Beta APIs are still on development and might not be available in a future version of Bitwig Studio.

 Turning this flag to true, will flag your extension as being a beta extension which might not work after
 updating Bitwig Studio.

**Returns:** type boolean - true if the extension wants to use Beta APIs.

### getHelpFilePath()

Gets a remote URI or a path within the extension's jar file where documentation for this extension can
 be found or null if there is none. If the path is not a URI then it is assumed to be a path below the directory
 "Documentation" within the extension's jar file.

**Returns:** type String

### getSupportFolderPath()

Gets a remote URI or a path within the extension's jar file where support files for this extension can
 be found or null if there is none. If the path is not a URI then it is assumed to be a path below the directory
 "Documentation" within the extension's jar file.

 Support files are for example a configuration file that one has use with a configuration software.

**Returns:** type String

### shouldFailOnDeprecatedUse()

If true then this extension should fail when it calls a deprecated method in the API. This is useful
 during development.

**Returns:** type boolean

### getErrorReportingEMail()

An e-mail address that can be used to contact the author of this extension if a problem is detected with
 it or null if none.

**Returns:** type String

