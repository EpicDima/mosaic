package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.layout.Placeable
import com.jakewharton.mosaic.layout.Placeable.PlacementScope

internal fun interface DrawPolicy {
	fun performDraw(canvas: TextCanvas)
}

internal fun interface StaticPaintPolicy {
	fun MosaicNode.performPaintStatics(statics: MutableList<TextSurface>)

	companion object {
		val Children = StaticPaintPolicy { statics ->
			for (child in children) {
				child.paintStatics(statics)
			}
		}
	}
}

internal fun interface DebugPolicy {
	fun MosaicNode.renderDebug(): String
}

internal abstract class MosaicNodeLayer : Placeable(), PlacementScope, MeasureScope {
	abstract fun measure(): MeasureResult
	abstract fun drawTo(canvas: TextCanvas)
}

internal abstract class AbstractMosaicNodeLayer(
	private val next: MosaicNodeLayer?,
) : MosaicNodeLayer() {
	private var measureResult: MeasureResult = NotMeasured

	final override val width get() = measureResult.width
	final override val height get() = measureResult.height

	final override fun measure(): MeasureResult {
		return doMeasure().also { measureResult = it }
	}

	open fun doMeasure(): MeasureResult {
		return checkNotNull(next).measure()
	}

	final override var x = 0
		private set
	final override var y = 0
		private set

	final override fun placeAt(x: Int, y: Int) {
		this.x = x
		this.y = y
		measureResult.placeChildren()
	}

	final override fun drawTo(canvas: TextCanvas) {
		drawLayer(canvas)
		next?.drawTo(canvas)
	}

	open fun drawLayer(canvas: TextCanvas) {}
}

internal object NotMeasured : MeasureResult {
	override val width get() = 0
	override val height get() = 0
	override fun placeChildren() = throw UnsupportedOperationException("Not measured")
}

internal class MosaicNode(
	var measurePolicy: MeasurePolicy,
	var staticPaintPolicy: StaticPaintPolicy?,
	drawPolicy: DrawPolicy?,
	var debugPolicy: DebugPolicy,
) : Measurable {
	val children = mutableListOf<MosaicNode>()

	private val bottomLayer: MosaicNodeLayer = object : AbstractMosaicNodeLayer(null) {
		override fun doMeasure(): MeasureResult {
			return measurePolicy.run { measure(children) }
		}

		override fun drawLayer(canvas: TextCanvas) {
			for (child in children) {
				if (child.width != 0 && child.height != 0) {
					child.topLayer.drawTo(canvas)
				}
			}
		}
	}

	private var topLayer = bottomLayer

	var drawPolicy: DrawPolicy? = drawPolicy
		set(value) {
			topLayer = if (value == null) {
				bottomLayer
			} else {
				object : AbstractMosaicNodeLayer(bottomLayer) {
					override fun drawLayer(canvas: TextCanvas) {
						canvas.translationX += x
						canvas.translationY += y
						value.performDraw(canvas)
						canvas.translationX -= x
						canvas.translationY -= y
					}
				}
			}
			field = value
		}

	override fun measure(): Placeable = topLayer.apply { measure() }

	val width: Int get() = topLayer.width
	val height: Int get() = topLayer.height
	val x: Int get() = topLayer.x
	val y: Int get() = topLayer.y

	fun measureAndPlace() {
		val placeable = measure()
		topLayer.run { placeable.place(0, 0) }
	}

	/**
	 * Draw this node to a [TextSurface].
	 * A call to [measureAndPlace] must precede calls to this function.
	 */
	fun paint(): TextSurface {
		val surface = TextSurface(width, height)
		topLayer.drawTo(surface)
		return surface
	}

	/**
	 * Append any static [TextSurfaces][TextSurface] to [statics].
	 * A call to [measureAndPlace] must precede calls to this function.
	 */
	fun paintStatics(statics: MutableList<TextSurface>) = staticPaintPolicy?.run { performPaintStatics(statics) }

	override fun toString() = debugPolicy.run { renderDebug() }

	companion object {
		val Factory: () -> MosaicNode = {
			MosaicNode(
				measurePolicy = ThrowingPolicy,
				drawPolicy = ThrowingPolicy,
				staticPaintPolicy = ThrowingPolicy,
				debugPolicy = ThrowingPolicy,
			)
		}

		fun root(): MosaicNode {
			return MosaicNode(
				measurePolicy = { measurables ->
					var width = 0
					var height = 0
					val placeables = measurables.map { measurable ->
						measurable.measure().also {
							width = maxOf(width, it.width)
							height = maxOf(height, it.height)
						}
					}
					layout(width, height) {
						for (placeable in placeables) {
							placeable.place(0, 0)
						}
					}
				},
				drawPolicy = null,
				staticPaintPolicy = StaticPaintPolicy.Children,
				debugPolicy = {
					children.joinToString(separator = "\n")
				}
			)
		}

		private val ThrowingPolicy = object : MeasurePolicy, DrawPolicy, StaticPaintPolicy, DebugPolicy {
			override fun MeasureScope.measure(measurables: List<Measurable>) = throw AssertionError()
			override fun performDraw(canvas: TextCanvas) = throw AssertionError()
			override fun MosaicNode.performPaintStatics(statics: MutableList<TextSurface>) = throw AssertionError()
			override fun MosaicNode.renderDebug() = throw AssertionError()
		}
	}
}

@Composable
internal inline fun Node(
	content: @Composable () -> Unit = {},
	measurePolicy: MeasurePolicy,
	drawPolicy: DrawPolicy?,
	staticPaintPolicy: StaticPaintPolicy?,
	debugPolicy: DebugPolicy,
) {
	ReusableComposeNode<MosaicNode, Applier<Any>>(
		factory = MosaicNode.Factory,
		update = {
			set(measurePolicy) { this.measurePolicy = measurePolicy }
			set(drawPolicy) { this.drawPolicy = drawPolicy }
			set(staticPaintPolicy) { this.staticPaintPolicy = staticPaintPolicy }
			set(debugPolicy) { this.debugPolicy = debugPolicy }
		},
		content = content,
	)
}

internal class MosaicNodeApplier(root: MosaicNode) : AbstractApplier<MosaicNode>(root) {
	override fun insertTopDown(index: Int, instance: MosaicNode) {
		// Ignored, we insert bottom-up.
	}

	override fun insertBottomUp(index: Int, instance: MosaicNode) {
		current.children.add(index, instance)
	}

	override fun remove(index: Int, count: Int) {
		current.children.remove(index, count)
	}

	override fun move(from: Int, to: Int, count: Int) {
		current.children.move(from, to, count)
	}

	override fun onClear() {}
}
