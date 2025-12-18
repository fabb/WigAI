# RemoteConnection

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Instances of this interface are reported to the supplied script callback when connecting to a remote TCP
 socket via ControllerHost.connectToRemoteHost(java.lang.String, int, com.bitwig.extension.callback.ConnectionEstablishedCallback).

## Methods

### disconnect()

Disconnects from the remote host.

**Returns:** type void

### setDisconnectCallback(com.bitwig.extension.callback.NoArgsCallback)

Registers a callback function that gets called when the connection gets lost or disconnected.

**Parameters:**
- `callback` (type NoArgsCallback): a callback function that doesn't receive any parameters

**Returns:** type void

### setReceiveCallback(com.bitwig.extension.callback.DataReceivedCallback)

Sets the callback used for receiving data.

 The remote connection needs a header for each message sent to it containing a 32-bit big-endian integer
 saying how big the rest of the message is. The data delivered to the script will not include this
 header.

**Parameters:**
- `callback` (type DataReceivedCallback): a callback function with the signature `(byte[] data)`

**Returns:** type void

### send(byte[])

Sends data to the remote host.

**Parameters:**
- `data` (type byte[]): the byte array containing the data to be sent. When creating a numeric byte array in
           JavaScript, the byte values must be signed (in the range -128..127).

**Returns:** type void

