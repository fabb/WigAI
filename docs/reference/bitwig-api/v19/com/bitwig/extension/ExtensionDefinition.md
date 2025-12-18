# ExtensionDefinition

- Kind: abstract class
- Package: `com.bitwig.extension`

Base class for defining any kind of extension for Bitwig Studio.

**Extends:** java.lang.Object

## Methods

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

### toString()

**Returns:** type String

