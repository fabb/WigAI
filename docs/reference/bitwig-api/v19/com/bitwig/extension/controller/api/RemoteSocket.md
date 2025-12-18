# RemoteSocket

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface represent a TCP socket that other network clients can connect to, typically
 created by calling ControllerHost.createRemoteConnection(java.lang.String, int).

## Methods

### setClientConnectCallback(com.bitwig.extension.callback.ConnectionEstablishedCallback)

Sets a callback which receives a remote connection object for each incoming connection.

**Parameters:**
- `callback` (type ConnectionEstablishedCallback): a callback function which receives a single

**Returns:** type void

### getPort()

Gets the actual port used for the remote socket, which might differ from the originally requested port
 when calling ControllerHost.createRemoteConnection(String name, int port) in case the requested port was
 already used.

**Returns:** type int - the actual port used for the remote socket

