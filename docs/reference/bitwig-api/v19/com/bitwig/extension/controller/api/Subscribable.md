# Subscribable

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Interface for an object that can be 'subscribed' or not. A subscribed object will notify any observers when
 changes occur to it. When it is unsubscribed the observers will no longer be notified. A driver can use
 this to say which objects it is interested in and which ones it is not (for example in one mode the driver
 may not be interested in track meters) at runtime. This allows the driver to improve efficiency by only
 getting notified about changes that are really relevant to it. By default a driver is subscribed to
 everything.

 Subscription is counter based.

## Methods

### isSubscribed()

Determines if this object is currently 'subscribed'. In the subscribed state it will notify any
 observers registered on it.

**Returns:** type boolean

### setIsSubscribed(boolean)

Sets whether the driver currently considers this object 'active' or not.

**Parameters:**
- `value` (type boolean)

**Returns:** type void

### subscribe()

Subscribes the driver to this object.

**Returns:** type void

### unsubscribe()

Unsubscribes the driver from this object.

**Returns:** type void

