# AbsoluteHardwareValueMatcher

- Kind: interface
- Package: `com.bitwig.extension.controller.api`

Defines a means of recognizing when an absolute value is input by the user (for example, when moving a
 slider or turning a knob based on some MIDI message). This matcher can then be set on an
 AbsoluteHardwareControl using
 AbsoluteHardwareControl.setAdjustValueMatcher(AbsoluteHardwareValueMatcher).

**Extends:** `com.bitwig.extension.controller.api.ContinuousHardwareValueMatcher`

