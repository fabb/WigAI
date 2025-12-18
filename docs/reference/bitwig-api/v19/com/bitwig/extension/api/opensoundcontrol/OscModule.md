# OscModule

- Kind: interface
- Package: `com.bitwig.extension.api.opensoundcontrol`

Interface to create Osc related object.

## Methods

### createAddressSpace()

Creates a new OscAddressSpace.

 In short the OscAddressSpace dispatches the incoming messages to services.
 An OscAddressSpace is an OscService.

**Returns:** type OscAddressSpace

### createUdpServer(int,com.bitwig.extension.api.opensoundcontrol.OscAddressSpace)

Creates a new OSC Server.

**Parameters:**
- `port` (type int)
- `addressSpace` (type OscAddressSpace): Use

**Returns:** type void

### createUdpServer(com.bitwig.extension.api.opensoundcontrol.OscAddressSpace)

Creates a new OSC Server.
 This server is not started yet, you'll have to start it by calling server.start(port);
 Use this method if the port is not known during the initialization (coming from a setting)
 or if the port number can change at runtime.

**Parameters:**
- `addressSpace` (type OscAddressSpace): Use

**Returns:** type OscServer - a new OscServer

### connectToUdpServer(java.lang.String,int,com.bitwig.extension.api.opensoundcontrol.OscAddressSpace)

Tries to connect to an OscServer.

**Parameters:**
- `host` (type String)
- `port` (type int)
- `addressSpace` (type OscAddressSpace): can be null

**Returns:** type OscConnection - a new OscConnection

