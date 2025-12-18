# OscAddressSpace

- Kind: interface
- Package: `com.bitwig.extension.api.opensoundcontrol`

An OSC address space.

 It contains the root OscContainer.

## Methods

### registerObjectMethods(java.lang.String,java.lang.Object)

Register all the methods annotated with OscMethod object.
 Also, if a method is annotated with OscNode, this method will be called and the returned object's method
 will be registered.

**Parameters:**
- `addressPrefix` (type String)
- `object` (type Object)

**Returns:** type void

### registerMethod(java.lang.String,java.lang.String,java.lang.String,com.bitwig.extension.api.opensoundcontrol.OscMethodCallback)

Low level way to register an Osc Method.

**Parameters:**
- `address` (type String): The address to register the method at
- `typeTagPattern` (type String): The globing pattern used to match the type tag. Pass "*" to match anything.
- `desc` (type String): The method description.
- `callback` (type OscMethodCallback): The OSC Method call handler.

**Returns:** type void

### registerDefaultMethod(com.bitwig.extension.api.opensoundcontrol.OscMethodCallback)

This method will be called if no registered OscMethod could handle incoming OscPacket.

**Parameters:**
- `callback` (type OscMethodCallback)

**Returns:** type void

### setShouldLogMessages(boolean)

Should the address spaces log the messages it dispatches?
 Default is false.

**Parameters:**
- `shouldLogMessages` (type boolean)

**Returns:** type void

### setName(java.lang.String)

This gives a display name for this address space.
 It is useful if you have multiple address space to identify them when we generate the documentation.

**Parameters:**
- `name` (type String)

**Returns:** type void

