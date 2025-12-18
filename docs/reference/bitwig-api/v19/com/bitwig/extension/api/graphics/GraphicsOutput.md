# GraphicsOutput

- Kind: interface
- Package: `com.bitwig.extension.api.graphics`

Provides 2D vector drawing API very similar to cairo graphics.
 Please read https://www.cairographics.org/manual/ to get a better idea of how this API works.

## Methods

### save()

**Returns:** type void

### restore()

**Returns:** type void

### clip()

**Returns:** type void

### clipPreserve()

**Returns:** type void

### resetClip()

**Returns:** type void

### translate(double,double)

**Parameters:**
- `x` (type double)
- `y` (type double)

**Returns:** type void

### rotate(double)

**Parameters:**
- `angle` (type double)

**Returns:** type void

### scale(double)

**Parameters:**
- `factor` (type double)

**Returns:** type void

### scale(double,double)

**Parameters:**
- `xFactor` (type double)
- `yFactor` (type double)

**Returns:** type void

### newPath()

**Returns:** type void

### newSubPath()

**Returns:** type void

### copyPath()

**Returns:** type Path

### copyPathFlat()

**Returns:** type Path

### appendPath(com.bitwig.extension.api.graphics.Path)

**Parameters:**
- `path` (type Path)

**Returns:** type void

### closePath()

**Returns:** type void

### moveTo(double,double)

**Parameters:**
- `x` (type double)
- `y` (type double)

**Returns:** type void

### relMoveTo(double,double)

**Parameters:**
- `x` (type double)
- `y` (type double)

**Returns:** type void

### lineTo(double,double)

**Parameters:**
- `x` (type double)
- `y` (type double)

**Returns:** type void

### relLineTo(double,double)

**Parameters:**
- `x` (type double)
- `y` (type double)

**Returns:** type void

### rectangle(double,double,double,double)

**Parameters:**
- `x` (type double)
- `y` (type double)
- `width` (type double)
- `height` (type double)

**Returns:** type void

### arc(double,double,double,double,double)

**Parameters:**
- `xc` (type double)
- `yc` (type double)
- `radius` (type double)
- `angle1` (type double)
- `angle2` (type double)

**Returns:** type void

### arcNegative(double,double,double,double,double)

**Parameters:**
- `xc` (type double)
- `yc` (type double)
- `radius` (type double)
- `angle1` (type double)
- `angle2` (type double)

**Returns:** type void

### circle(double,double,double)

**Parameters:**
- `centerX` (type double)
- `centerY` (type double)
- `radius` (type double)

**Returns:** type void

### curveTo(double,double,double,double,double,double)

**Parameters:**
- `x1` (type double)
- `y1` (type double)
- `x2` (type double)
- `y2` (type double)
- `x3` (type double)
- `y3` (type double)

**Returns:** type void

### relCurveTo(double,double,double,double,double,double)

**Parameters:**
- `x1` (type double)
- `y1` (type double)
- `x2` (type double)
- `y2` (type double)
- `x3` (type double)
- `y3` (type double)

**Returns:** type void

### paint()

**Returns:** type void

### paintWithAlpha(double)

**Parameters:**
- `alpha` (type double)

**Returns:** type void

### mask(com.bitwig.extension.api.graphics.Image,double,double)

**Parameters:**
- `image` (type Image)
- `x` (type double)
- `y` (type double)

**Returns:** type void

### fill()

**Returns:** type void

### fillPreserve()

**Returns:** type void

### stroke()

**Returns:** type void

### strokePreserve()

**Returns:** type void

### setColor(double,double,double)

**Parameters:**
- `red` (type double)
- `green` (type double)
- `blue` (type double)

**Returns:** type void

### setColor(double,double,double,double)

**Parameters:**
- `red` (type double)
- `green` (type double)
- `blue` (type double)
- `alpha` (type double)

**Returns:** type void

### setColor(com.bitwig.extension.api.Color)

**Parameters:**
- `color` (type Color)

**Returns:** type void

### setPattern(com.bitwig.extension.api.graphics.Pattern)

**Parameters:**
- `pattern` (type Pattern)

**Returns:** type void

### setAntialias(com.bitwig.extension.api.graphics.GraphicsOutput.AntialiasMode)

**Parameters:**
- `antialiasMode` (type GraphicsOutput.AntialiasMode)

**Returns:** type void

### setLineWidth(double)

**Parameters:**
- `width` (type double)

**Returns:** type void

### setDash(double[],double)

**Parameters:**
- `dashes` (type double[])
- `offset` (type double)

**Returns:** type void

### setDash(double[])

**Parameters:**
- `dashes` (type double[])

**Returns:** type void

### setFillRule(com.bitwig.extension.api.graphics.GraphicsOutput.FillRule)

**Parameters:**
- `rule` (type GraphicsOutput.FillRule)

**Returns:** type void

### setLineCap(com.bitwig.extension.api.graphics.GraphicsOutput.LineCap)

**Parameters:**
- `lineCap` (type GraphicsOutput.LineCap)

**Returns:** type void

### setLineJoin(com.bitwig.extension.api.graphics.GraphicsOutput.LineJoin)

**Parameters:**
- `lineJoin` (type GraphicsOutput.LineJoin)

**Returns:** type void

### setMiterLimit(double)

**Parameters:**
- `limit` (type double)

**Returns:** type void

### setOperator(com.bitwig.extension.api.graphics.GraphicsOutput.Operator)

**Parameters:**
- `operator` (type GraphicsOutput.Operator)

**Returns:** type void

### setTolerance(double)

**Parameters:**
- `tolerance` (type double)

**Returns:** type void

### drawImage(com.bitwig.extension.api.graphics.Image,double,double)

**Parameters:**
- `image` (type Image)
- `x` (type double)
- `y` (type double)

**Returns:** type void

### createLinearGradient(double,double,double,double)

**Parameters:**
- `x1` (type double)
- `y1` (type double)
- `x2` (type double)
- `y2` (type double)

**Returns:** type GradientPattern

### createMeshGradient()

**Returns:** type MeshPattern

### showText(java.lang.String)

**Parameters:**
- `text` (type String)

**Returns:** type void

### setFontSize(double)

**Parameters:**
- `fontSize` (type double)

**Returns:** type void

### setFontFace(com.bitwig.extension.api.graphics.FontFace)

**Parameters:**
- `fontFace` (type FontFace)

**Returns:** type void

### setFontOptions(com.bitwig.extension.api.graphics.FontOptions)

**Parameters:**
- `fontOptions` (type FontOptions)

**Returns:** type void

### getFontExtents()

**Returns:** type FontExtents

### getTextExtents(java.lang.String)

**Parameters:**
- `text` (type String)

**Returns:** type TextExtents

