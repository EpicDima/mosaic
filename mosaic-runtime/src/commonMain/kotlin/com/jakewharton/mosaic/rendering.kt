package com.jakewharton.mosaic

import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.ui.AnsiLevel
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal interface Rendering {
	/**
	 * Render [node] to a single string for display.
	 *
	 * Note: The returned [CharSequence] is only valid until the next call to this function,
	 * as implementations are free to reuse buffers across invocations.
	 */
	fun render(node: MosaicNode): CharSequence
}

@ExperimentalTime
internal class DebugRendering(
	private val systemClock: TimeSource = TimeSource.Monotonic,
	ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
) : Rendering {
	private var lastRender: TimeMark? = null
	private val staticTextSurface = TextSurface(ansiLevel)
	private val textSurface = TextSurface(ansiLevel)

	override fun render(node: MosaicNode): CharSequence {
		var failed = false
		val output = buildString {
			lastRender?.let { lastRender ->
				repeat(50) { append('~') }
				append(" +")
				appendLine(lastRender.elapsedNow())
			}
			lastRender = systemClock.markNow()

			node.measureAndPlace()
			appendLine("NODES:")
			appendLine(node)
			appendLine()

			try {
				staticTextSurface.reset()
				node.paintStatics(staticTextSurface)
				if (staticTextSurface.width > 0 && staticTextSurface.height > 0) {
					appendLine("STATIC:")
					appendLine(staticTextSurface.render())
					appendLine()
				}
			} catch (t: Throwable) {
				failed = true
				appendLine("STATIC:")
				appendLine(t.stackTraceToString())
			}

			appendLine("OUTPUT:")
			try {
				textSurface.reset()
				textSurface.resize(node.width, node.height)
				node.paint(textSurface)
				appendLine(textSurface.render())
			} catch (t: Throwable) {
				failed = true
				append(t.stackTraceToString())
			}
		}
		if (failed) {
			throw RuntimeException("Failed\n\n$output")
		}
		return output
	}
}

internal class AnsiRendering(
	ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
) : Rendering {
	private val stringBuilder = StringBuilder(128)

	private val staticTextSurface = TextSurface(ansiLevel)
	private val textSurface = TextSurface(ansiLevel)

	override fun render(node: MosaicNode): CharSequence {
		return stringBuilder.apply {
			clear()

			append(ansiBeginSynchronizedUpdate)

			// don't need to move cursor up if there was zero or one line
			if (textSurface.height > 1) {
				ansiMoveCursorUp(textSurface.height - 1)
			}
			append(ansiMoveCursorToFirstColumn)

			node.measureAndPlace()

			var afterStatic = false // in order not to overwrite last line of static output
			staticTextSurface.reset()
			node.paintStatics(staticTextSurface) // canvas size change inside
			if (staticTextSurface.height > 0 && staticTextSurface.width > 0) {
				appendSurface(staticTextSurface, addLineBreakAtBeginning = false)
				afterStatic = true
			}

			if (node.width > 0 && node.height > 0) {
				textSurface.reset()
				textSurface.resize(node.width, node.height)
				node.paint(textSurface)
				appendSurface(textSurface, addLineBreakAtBeginning = afterStatic)
			}

			append(ansiClearAllAfterCursor)
			append(ansiEndSynchronizedUpdate)
		}
	}

	private fun StringBuilder.appendSurface(canvas: TextSurface, addLineBreakAtBeginning: Boolean) {
		for (rowIndex in 0 until canvas.height) {
			if (rowIndex > 0 || (rowIndex == 0 && addLineBreakAtBeginning)) {
				append("\r\n")
			}
			canvas.appendRowTo(this, rowIndex)
			append(ansiClearLineAfterCursor)
		}
	}
}
