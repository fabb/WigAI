# ClipLauncherSlotBankPlaybackStateChangedCallback

- Kind: interface
- Package: `com.bitwig.extension.callback`

**Extends:** `com.bitwig.extension.callback.Callback`

## Methods

### playbackStateChanged(int,int,boolean)

Registers an observer that reports the playback state of clips / slots. The reported states include
 `stopped`, `playing`, `recording`, but also `queued for stop`, `queued for playback`, `queued for
 recording`.

**Parameters:**
- `slotIndex` (type int)
- `playbackState` (type int): the queued or playback state: `0` when stopped, `1` when playing, or `2` when recording
- `isQueued` (type boolean): indicates if the second argument is referring to the queued state (`true`) or the actual playback state (`false`)

**Returns:** type void

