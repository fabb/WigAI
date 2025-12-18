# NotificationSettings

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Bitwig Studio supports automatic visual feedback from controllers that shows up as popup notifications. For
 example when the selected track or the current device preset was changed on the controller, these
 notifications are shown, depending on the configuration.

 It depends both on the users preference and the capabilities of the controller hardware if a certain
 notification should be shown. This interface provides functions for enabling/disabling the various kinds of
 automatic notifications from the hardware point of view. Typically, controllers that include an advanced
 display don't need to show many notifications additionally on screen. For other controllers that do not
 include a display it might be useful to show all notifications. By default all notifications are disabled.

 In addition, the user can enable or disable all notifications the have been enabled using this interface in
 the preferences dialog of Bitwig Studio.

## Methods

### getUserNotificationsEnabled()

Returns an object that reports if user notifications are enabled and that allows to enable/disable user
 notifications from the control surface. If user notifications are disabled, no automatic notifications
 will be shown in the Bitwig Studio user interface. If user notifications are enabled, all automatic
 notifications will be shown that are enabled using the methods of this interface.

**Returns:** type SettableBooleanValue - a boolean value object

### setShouldShowSelectionNotifications(boolean)

Specifies if user notification related to selection changes should be shown. Please note that this
 setting only applies when user notifications are enabled in general, otherwise no notification are
 shown. By default this setting is `false`.

**Parameters:**
- `shouldShowNotifications` (type boolean): `true` in case selection notifications should be shown, `false` otherwise.

**Returns:** type void

### setShouldShowChannelSelectionNotifications(boolean)

Specifies if user notification related to selection changes should be shown. Please note that this
 setting only applies when user notifications are enabled in general, otherwise no notification are
 shown. By default this setting is `false`.

**Parameters:**
- `shouldShowNotifications` (type boolean): `true` in case selection notifications should be shown, `false` otherwise.

**Returns:** type void

### setShouldShowTrackSelectionNotifications(boolean)

Specifies if user notification related to selection changes should be shown. Please note that this
 setting only applies when user notifications are enabled in general, otherwise no notification are
 shown. By default this setting is `false`.

**Parameters:**
- `shouldShowNotifications` (type boolean): `true` in case selection notifications should be shown, `false` otherwise.

**Returns:** type void

### setShouldShowDeviceSelectionNotifications(boolean)

Specifies if user notification related to selection changes should be shown. Please note that this
 setting only applies when user notifications are enabled in general, otherwise no notification are
 shown. By default this setting is `false`.

**Parameters:**
- `shouldShowNotifications` (type boolean): `true` in case selection notifications should be shown, `false` otherwise.

**Returns:** type void

### setShouldShowDeviceLayerSelectionNotifications(boolean)

Specifies if user notification related to selection changes should be shown. Please note that this
 setting only applies when user notifications are enabled in general, otherwise no notification are
 shown. By default this setting is `false`.

**Parameters:**
- `shouldShowNotifications` (type boolean): `true` in case selection notifications should be shown, `false` otherwise.

**Returns:** type void

### setShouldShowPresetNotifications(boolean)

Specifies if user notification related to selection changes should be shown. Please note that this
 setting only applies when user notifications are enabled in general, otherwise no notification are
 shown.

**Parameters:**
- `shouldShowNotifications` (type boolean): `true` in case selection notifications should be shown, `false` otherwise.

**Returns:** type void

### setShouldShowMappingNotifications(boolean)

Specifies if user notification related to selection changes should be shown. Please note that this
 setting only applies when user notifications are enabled in general, otherwise no notification are
 shown. By default this setting is `false`.

**Parameters:**
- `shouldShowNotifications` (type boolean): `true` in case selection notifications should be shown, `false` otherwise.

**Returns:** type void

### setShouldShowValueNotifications(boolean)

Specifies if user notification related to selection changes should be shown. Please note that this
 setting only applies when user notifications are enabled in general, otherwise no notification are
 shown. By default this setting is `false`.

**Parameters:**
- `shouldShowNotifications` (type boolean): `true` in case selection notifications should be shown, `false` otherwise.

**Returns:** type void

