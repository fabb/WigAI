# FontExtents

- Kind: interface
- Package: `com.bitwig.extension.api.graphics`

Information about the dimensions of a font.

## Methods

### getAscent()

Returns the distance that the font extends above the baseline. Note that this is not always
 exactly equal to the maximum of the extents of all the glyphs in the font, but rather is
 picked to express the font designer's intent as to how the font should align with elements
 above it.

**Returns:** type double

### getDescent()

Returns the distance that the font extends below the baseline. This value is positive for
 typical fonts that include portions below the baseline. Note that this is not always exactly
 equal to the maximum of the extents of all the glyphs in the font, but rather is picked to
 express the font designer's intent as to how the the font should align with elements below it.

**Returns:** type double

### getHeight()

Returns the recommended vertical distance between baselines when setting consecutive lines of
 text with the font. This is greater than ascent+descent by a quantity known as the line
 spacing or external leading. When space is at a premium, most fonts can be set with only a
 distance of ascent+descent between lines.

**Returns:** type double

### getMaxAdvanceX()

the maximum distance in the X direction that the the origin is advanced for any glyph in the
 font.

**Returns:** type double

### getMaxAdvanceY()

Returns the maximum distance in the Y direction that the the origin is advanced for any glyph
 in the font. this will be zero for normal fonts used for horizontal writing. (The scripts of
 East Asia are sometimes written vertically.)

**Returns:** type double

