# OscBundle

- Kind: interface
- Package: `com.bitwig.extension.api.opensoundcontrol`

An OSC Bundle.

**Extends:** `com.bitwig.extension.api.opensoundcontrol.OscPacket`

## Methods

### getNanoseconds()

**Returns:** type long

### getPackets()

**Returns:** type List<OscPacket>

### getParentBundle()

If the message was part of a bundle, get a pointer back to it.
 If not, this methods returns null.

**Returns:** type OscBundle

