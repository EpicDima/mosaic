@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic.ui.unit

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min

/**
 * Constructs an [IntSize] from width and height [Int] values.
 */
@Stable
public fun IntSize(width: Int, height: Int): IntSize = IntSize(packInts(width, height))

/**
 * A two-dimensional size class used for measuring in [Int] cells.
 */
@Immutable
@JvmInline
public value class IntSize internal constructor(@PublishedApi internal val packedValue: Long) {

	/**
	 * The horizontal aspect of the size in [Int] cells.
	 */
	@Stable
	public val width: Int
		get() = unpackInt1(packedValue)

	/**
	 * The vertical aspect of the size in [Int] cells.
	 */
	@Stable
	public val height: Int
		get() = unpackInt2(packedValue)

	@Stable
	public inline operator fun component1(): Int = width

	@Stable
	public inline operator fun component2(): Int = height

	/**
	 * Returns an IntSize scaled by multiplying [width] and [height] by [other]
	 */
	@Stable
	public operator fun times(other: Int): IntSize =
		IntSize(width = width * other, height = height * other)

	/**
	 * Returns an IntSize scaled by dividing [width] and [height] by [other]
	 */
	@Stable
	public operator fun div(other: Int): IntSize =
		IntSize(width = width / other, height = height / other)

	/**
	 * The lesser of the magnitudes of the [width] and the [height].
	 */
	@Stable
	public val minDimension: Int
		get() = min(width, height)

	/**
	 * The greater of the magnitudes of the [width] and the [height].
	 */
	@Stable
	public val maxDimension: Int
		get() = max(width, height)

	@Stable
	override fun toString(): String = "$width x $height"

	public companion object {
		/**
		 * IntSize with a zero (0) width and height.
		 */
		public val Zero: IntSize = IntSize(0L)
	}
}

/**
 * Returns the [IntOffset] of the center of the rect from the point of [0, 0]
 * with this [IntSize].
 */
@Stable
public val IntSize.center: IntOffset
	get() = IntOffset(
		// Divide X by 2 by moving it to the low bits, then place it back in the high bits
		(packedValue shr 33 shl 32)
			or
			// Move Y to the high bits so we can preserve the sign when dividing by 2, then
			// move Y back to the low bits and mask out the top 32 bits for X
			((packedValue shl 32 shr 33) and 0xffffffffL),
	)
