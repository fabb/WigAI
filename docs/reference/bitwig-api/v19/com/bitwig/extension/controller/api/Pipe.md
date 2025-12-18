# Pipe

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

A pipe represents a communication channel with some other hardware device or network service. Pipes are
 opened and closed by Bitwig Studio automatically and exist for the entire lifetime of a controller. If
 communication is lost on a specific pipe (for example the user unplugs a USB device) then the controller
 will exit and the user will be notified.

 A controller defines which pipes it wants to establish for communication using a
 HardwareDeviceMatcher.

