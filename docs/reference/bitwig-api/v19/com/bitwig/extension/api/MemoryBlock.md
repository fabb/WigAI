# MemoryBlock

- Kind: interface
- Package: `com.bitwig.extension.api`

Defines a block of memory. The memory can be read/written using a ByteBuffer provided by
 createByteBuffer().

## Methods

### size()

The size in bytes of this memory block.

**Returns:** type int

### createByteBuffer()

Creates a ByteBuffer that can be used to read/write the data at this memory block.

**Returns:** type ByteBuffer

