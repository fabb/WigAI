# OutputPipe

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

A pipe that can be used to write data.

**Extends:** `com.bitwig.extension.controller.api.Pipe`

## Methods

### writeAsync(com.bitwig.extension.api.MemoryBlock,com.bitwig.extension.controller.api.AsyncTransferCompledCallback,int)

Requests to write some data to this pipe in an asynchronous way (the caller is not blocked). Once some
 data has been written the callback will be notified on the controller's thread.

**Parameters:**
- `data` (type MemoryBlock): A
- `callback` (type AsyncTransferCompledCallback): A callback that is notified on the controller's thread when the write has completed.
- `timeoutInMs` (type int): A timeout in milliseconds that will result in an error and termination of the controller if
           the write does not happen in this time. For infinite timeout use 0.

**Returns:** type void

### write(com.bitwig.extension.api.MemoryBlock,int)

**Parameters:**
- `data` (type MemoryBlock)
- `timeoutInMs` (type int)

**Returns:** type int

