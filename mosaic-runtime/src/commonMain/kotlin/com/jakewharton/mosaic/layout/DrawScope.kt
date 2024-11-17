/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.TextPixel
import com.jakewharton.mosaic.TextSurface
import com.jakewharton.mosaic.UnspecifiedCodePoint
import com.jakewharton.mosaic.isSpecifiedCodePoint
import com.jakewharton.mosaic.isUnspecifiedCodePoint
import com.jakewharton.mosaic.text.AnnotatedString
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.getLocalRawSpanStyles
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.TextStyle
import com.jakewharton.mosaic.ui.isSpecifiedColor
import com.jakewharton.mosaic.ui.isSpecifiedTextStyle
import com.jakewharton.mosaic.ui.isUnspecifiedColor
import com.jakewharton.mosaic.ui.isUnspecifiedTextStyle
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import com.jakewharton.mosaic.ui.unit.center
import de.cketti.codepoints.codePointAt
import kotlin.math.max
import kotlin.math.min

public interface DrawScope {
	public val width: Int
	public val height: Int

	private val size: IntSize get() = IntSize(width, height)

	/**
	 * Draws a circle at the provided center coordinate and radius. If no center point is provided
	 * the center of the bounds is used.
	 *
	 * @param char Char to be applied to the circle
	 * @param foreground Foreground color to be applied to the circle
	 * @param background Background color to be applied to the circle
	 * @param textStyle Text style to be applied to the circle
	 * @param radius Radius of the circle
	 * @param center Center coordinate where the circle is to be drawn
	 * @param drawStyle Whether or not the circle is stroked or filled in
	 */
	public fun drawCircle(
		char: Char,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
		radius: Int = size.minDimension / 2,
		center: IntOffset = size.center,
		drawStyle: DrawStyle = DrawStyle.Fill,
	)

	/**
	 * Draws a circle at the provided center coordinate and radius. If no center point is provided
	 * the center of the bounds is used.
	 *
	 * @param codePoint Code point to be applied to the circle
	 * @param foreground Foreground color to be applied to the circle
	 * @param background Background color to be applied to the circle
	 * @param textStyle Text style to be applied to the circle
	 * @param radius Radius of the circle
	 * @param center Center coordinate where the circle is to be drawn
	 * @param drawStyle Whether or not the circle is stroked or filled in
	 */
	public fun drawCircle(
		codePoint: Int = UnspecifiedCodePoint,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
		radius: Int = size.minDimension / 2,
		center: IntOffset = size.center,
		drawStyle: DrawStyle = DrawStyle.Fill,
	)

	/**
	 * Draws a rectangle with the given offset and size. If no offset from the top left is provided,
	 * it is drawn starting from the origin of the current translation. If no size is provided,
	 * the size of the current environment is used.
	 *
	 * @param char Char to be applied to the rectangle
	 * @param foreground Foreground color to be applied to the rectangle
	 * @param background Background color to be applied to the rectangle
	 * @param textStyle Text style color to be applied to the rectangle
	 * @param topLeft Offset from the local origin of 0, 0 relative to the current translation
	 * @param size Dimensions of the rectangle to draw
	 * @param drawStyle Whether or not the rectangle is stroked or filled in
	 */
	public fun drawRect(
		char: Char,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
		topLeft: IntOffset = IntOffset.Zero,
		size: IntSize = this.size.offsetSize(topLeft),
		drawStyle: DrawStyle = DrawStyle.Fill,
	)

	/**
	 * Draws a rectangle with the given offset and size. If no offset from the top left is provided,
	 * it is drawn starting from the origin of the current translation. If no size is provided,
	 * the size of the current environment is used.
	 *
	 * @param codePoint Code point to be applied to the rectangle
	 * @param foreground Foreground color to be applied to the rectangle
	 * @param background Background color to be applied to the rectangle
	 * @param textStyle Text style color to be applied to the rectangle
	 * @param topLeft Offset from the local origin of 0, 0 relative to the current translation
	 * @param size Dimensions of the rectangle to draw
	 * @param drawStyle Whether or not the rectangle is stroked or filled in
	 */
	public fun drawRect(
		codePoint: Int = UnspecifiedCodePoint,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
		topLeft: IntOffset = IntOffset.Zero,
		size: IntSize = this.size.offsetSize(topLeft),
		drawStyle: DrawStyle = DrawStyle.Fill,
	)

	public fun drawText(
		row: Int,
		column: Int,
		string: String,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
	)

	public fun drawText(
		row: Int,
		column: Int,
		string: AnnotatedString,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
	)

	/**
	 * Helper method to offset the provided size with the offset in box width and height
	 */
	private fun IntSize.offsetSize(offset: IntOffset): IntSize =
		IntSize(this.width - offset.x, this.height - offset.y)
}

internal open class TextCanvasDrawScope(
	private val canvas: TextSurface,
	override val width: Int,
	override val height: Int,
) : DrawScope {
	override fun drawCircle(
		char: Char,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		radius: Int,
		center: IntOffset,
		drawStyle: DrawStyle,
	) {
		drawCircle(char.code, foreground, background, textStyle, radius, center, drawStyle)
	}

	override fun drawCircle(
		codePoint: Int,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		radius: Int,
		center: IntOffset,
		drawStyle: DrawStyle,
	) {
		if (codePoint.isUnspecifiedCodePoint &&
			foreground.isUnspecifiedColor &&
			background.isUnspecifiedColor &&
			textStyle.isUnspecifiedTextStyle ||
			radius <= 0 ||
			center.x + radius < 0 &&
			center.y + radius < 0 ||
			center.x - radius >= width &&
			center.y - radius >= height
		) {
			// exit: circle with the specified parameters cannot be seen
			return
		}

		when (drawStyle) {
			is DrawStyle.Fill -> drawSolidCircle(
				codePoint,
				foreground,
				background,
				textStyle,
				radius,
				center,
			)

			is DrawStyle.Stroke -> {
				val clippedStrokeWidth = max(1, drawStyle.width)
				val adaptedForStrokeRadius = clippedStrokeWidth / 2 + radius
				if (clippedStrokeWidth / 2 >= radius) {
					drawSolidCircle(
						codePoint,
						foreground,
						background,
						textStyle,
						adaptedForStrokeRadius,
						center,
					)
					return
				}

				val strokeWidth = clippedStrokeWidth - 1
				var x = 0
				var y = adaptedForStrokeRadius
				var d = 3 - 2 * adaptedForStrokeRadius

				drawStrokedCirclePart(
					x,
					y,
					codePoint,
					foreground,
					background,
					textStyle,
					center,
					strokeWidth,
				)
				while (y >= x) {
					x++
					if (d > 0) {
						y--
						d += 4 * (x - y) + 10
					} else {
						d += 4 * x + 6
					}
					drawStrokedCirclePart(
						x,
						y,
						codePoint,
						foreground,
						background,
						textStyle,
						center,
						strokeWidth,
					)
				}
			}
		}
	}

	private inline fun drawStrokedCirclePart(
		x: Int,
		y: Int,
		codePoint: Int,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		center: IntOffset,
		strokeWidth: Int,
	) {
		val x1 = center.x + x
		val x2 = center.x - x
		val x1Correct = x1 in 0..<width
		val x2Correct = x2 in 0..<width
		if (x1Correct || x2Correct) {
			val start1 = min(height - 1, center.y + y)
			val end1 = max(0, center.y + y - strokeWidth)
			for (yy in start1 downTo end1) {
				if (x1Correct) {
					drawTextPixel(x1, yy, codePoint, foreground, background, textStyle)
				}
				if (x2Correct) {
					drawTextPixel(x2, yy, codePoint, foreground, background, textStyle)
				}
			}

			val start2 = min(height - 1, center.y - y + strokeWidth)
			val end2 = max(0, center.y - y)
			for (yy in start2 downTo end2) {
				if (x1Correct) {
					drawTextPixel(x1, yy, codePoint, foreground, background, textStyle)
				}
				if (x2Correct) {
					drawTextPixel(x2, yy, codePoint, foreground, background, textStyle)
				}
			}
		}

		val y1 = center.y + x
		val y2 = center.y - x
		val y1Correct = y1 in 0..<height
		val y2Correct = y2 in 0..<height
		if (y1Correct || y2Correct) {
			val start3 = min(width - 1, center.x + y)
			val end3 = max(0, center.x + y - strokeWidth)
			for (xx in start3 downTo end3) {
				if (y1Correct) {
					drawTextPixel(xx, y1, codePoint, foreground, background, textStyle)
				}
				if (y2Correct) {
					drawTextPixel(xx, y2, codePoint, foreground, background, textStyle)
				}
			}

			val start4 = min(width - 1, center.x - y + strokeWidth)
			val end4 = max(0, center.x - y)
			for (xx in start4 downTo end4) {
				if (y1Correct) {
					drawTextPixel(xx, y1, codePoint, foreground, background, textStyle)
				}
				if (y2Correct) {
					drawTextPixel(xx, y2, codePoint, foreground, background, textStyle)
				}
			}
		}
	}

	private fun drawSolidCircle(
		codePoint: Int,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		radius: Int,
		center: IntOffset,
	) {
		var x = 0
		var y = radius
		var d = 3 - 2 * radius

		drawSolidCirclePart(x, y, codePoint, foreground, background, textStyle, center)
		while (y >= x) {
			x++
			if (d > 0) {
				y--
				d += 4 * (x - y) + 10
			} else {
				d += 4 * x + 6
			}
			drawSolidCirclePart(x, y, codePoint, foreground, background, textStyle, center)
		}
	}

	private inline fun drawSolidCirclePart(
		x: Int,
		y: Int,
		codePoint: Int,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		center: IntOffset,
	) {
		val y11 = center.y + y
		val y12 = center.y - y
		val y11Correct = y11 in 0..<height
		val y12Correct = y12 in 0..<height
		if (y11Correct || y12Correct) {
			val start1 = max(0, center.x - x)
			val end1 = min(width - 1, center.x + x)
			for (xx in start1..end1) {
				if (y11Correct) {
					drawTextPixel(xx, y11, codePoint, foreground, background, textStyle)
				}
				if (y12Correct) {
					drawTextPixel(xx, y12, codePoint, foreground, background, textStyle)
				}
			}
		}

		val y21 = center.y + x
		val y22 = center.y - x
		val y21Correct = y21 in 0..<height
		val y22Correct = y22 in 0..<height
		if (y21Correct || y22Correct) {
			val start2 = max(0, center.x - y)
			val end2 = min(width - 1, center.x + y)
			for (xx in start2..end2) {
				if (y21Correct) {
					drawTextPixel(xx, y21, codePoint, foreground, background, textStyle)
				}
				if (y22Correct) {
					drawTextPixel(xx, y22, codePoint, foreground, background, textStyle)
				}
			}
		}
	}

	override fun drawRect(
		char: Char,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		topLeft: IntOffset,
		size: IntSize,
		drawStyle: DrawStyle,
	) {
		drawRect(char.code, foreground, background, textStyle, topLeft, size, drawStyle)
	}

	override fun drawRect(
		codePoint: Int,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		topLeft: IntOffset,
		size: IntSize,
		drawStyle: DrawStyle,
	) {
		if (codePoint.isUnspecifiedCodePoint &&
			foreground.isUnspecifiedColor &&
			background.isUnspecifiedColor &&
			textStyle.isUnspecifiedTextStyle ||
			size.width <= 0 ||
			size.height <= 0 ||
			topLeft.x >= width ||
			topLeft.y >= height ||
			topLeft.x + size.width < 0 ||
			topLeft.y + size.height < 0
		) {
			// exit: rectangle with the specified parameters cannot be seen
			return
		}

		when (drawStyle) {
			is DrawStyle.Fill -> drawSolidRect(codePoint, foreground, background, textStyle, topLeft, size)
			is DrawStyle.Stroke -> {
				val strokeWidth = max(1, drawStyle.width)
				if (strokeWidth * 2 >= size.width || strokeWidth * 2 >= size.height) {
					// fast path: stroke width is large, it turns out a full fill
					drawSolidRect(codePoint, foreground, background, textStyle, topLeft, size)
					return
				}

				// left line
				drawSolidRect(
					codePoint,
					foreground,
					background,
					textStyle,
					topLeft,
					IntSize(strokeWidth, size.height),
				)
				// top line
				drawSolidRect(
					codePoint,
					foreground,
					background,
					textStyle,
					IntOffset(topLeft.x + strokeWidth, topLeft.y),
					IntSize(size.width - strokeWidth * 2, strokeWidth),
				)
				// right line
				drawSolidRect(
					codePoint,
					foreground,
					background,
					textStyle,
					IntOffset(topLeft.x + size.width - strokeWidth, topLeft.y),
					IntSize(strokeWidth, size.height),
				)
				// bottom line
				drawSolidRect(
					codePoint,
					foreground,
					background,
					textStyle,
					IntOffset(topLeft.x + strokeWidth, topLeft.y + size.height - strokeWidth),
					IntSize(size.width - strokeWidth * 2, strokeWidth),
				)
			}
		}
	}

	private inline fun drawSolidRect(
		codePoint: Int,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		topLeft: IntOffset,
		size: IntSize,
	) {
		for (y in topLeft.y until topLeft.y + size.height) {
			for (x in topLeft.x until topLeft.x + size.width) {
				drawTextPixel(x, y, codePoint, foreground, background, textStyle)
			}
		}
	}

	override fun drawText(
		row: Int,
		column: Int,
		string: String,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
	) {
		drawText(row, column, string, foreground, background, textStyle, null)
	}

	override fun drawText(
		row: Int,
		column: Int,
		string: AnnotatedString,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
	) {
		drawText(row, column, string.text, foreground, background, textStyle) { start, end ->
			string.getLocalRawSpanStyles(start, end)
		}
	}

	private fun drawText(
		row: Int,
		column: Int,
		text: String,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		spanStylesProvider: ((start: Int, end: Int) -> List<SpanStyle>)?,
	) {
		var pixelIndex = 0
		var characterColumn = column
		while (pixelIndex < text.length) {
			val character = canvas[row, characterColumn++]

			val pixelEnd = if (text[pixelIndex].isHighSurrogate()) {
				pixelIndex + 2
			} else {
				pixelIndex + 1
			}

			character.updateTextPixel(text.codePointAt(pixelIndex), foreground, background, textStyle)
			spanStylesProvider?.invoke(pixelIndex, pixelEnd)?.forEach {
				character.updateTextPixel(UnspecifiedCodePoint, it.color, it.background, it.textStyle)
			}

			pixelIndex = pixelEnd
		}
	}

	private inline fun drawTextPixel(
		x: Int,
		y: Int,
		codePoint: Int = UnspecifiedCodePoint,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
	) {
		canvas[y, x].updateTextPixel(codePoint, foreground, background, textStyle)
	}

	private inline fun TextPixel.updateTextPixel(
		codePoint: Int,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
	) {
		if (codePoint.isSpecifiedCodePoint) {
			this.codePoint = codePoint
		}
		if (foreground.isSpecifiedColor) {
			this.foreground = foreground
		}
		if (background.isSpecifiedColor) {
			this.background = background
		}
		if (textStyle.isSpecifiedTextStyle) {
			this.textStyle = textStyle
		}
	}
}
