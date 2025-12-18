# OscConnection

- Kind: interface
- Package: `com.bitwig.extension.api.opensoundcontrol`

This interface lets you send OscMessage through an connection which can be via Tcp, Udp, or whatever.

 OscPackets are sent when all the startBundle() have a matching endBundle().
 If you call sendMessage() with startBundle() before, then the message will be sent directly.

 Our maximum packet size is 64K.

## Methods

### startBundle()

Starts an OscBundle.

**Returns:** type void

### sendMessage(java.lang.String,java.lang.Object...)

Supported object types:
 - Integer for int32
 - Long for int64
 - Float for float
 - Double for double
 - null for nil
 - Boolean for true and false
 - String for string
 - byte[] for blob

**Parameters:**
- `address` (type String)
- `args` (type Object...)

**Returns:** type void

### endBundle()

Finishes the previous bundle, and if it was not inside an other bundle, it will send the message
 directly.

**Returns:** type void

