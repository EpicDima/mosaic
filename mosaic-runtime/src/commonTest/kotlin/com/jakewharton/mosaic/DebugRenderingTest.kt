package com.jakewharton.mosaic

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsMatch
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.message
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Static
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalTime::class)
class DebugRenderingTest {
	private val timeSource = TestTimeSource()
	private val rendering = DebugRendering(timeSource)

	@Test fun drawFailureStillRendersMeasuredAndPlacedNodes() = runTest {
		runMosaicTest(withRenderSnapshots = false) {
			setContent {
				Row {
					Text("Hello ")
					Layout(modifier = Modifier.drawBehind { throw UnsupportedOperationException() }) {
						layout(5, 1)
					}
				}
			}
			assertFailure {
				rendering.render(awaitNodeSnapshot())
			}.isInstanceOf<RuntimeException>()
				.message()
				.isNotNull()
				.containsMatch(
					"""
					|Failed
					|
					|NODES:
					|Row\(arrangement=Arrangement#Start, alignment=Vertical\(bias=-1\)\) x=0 y=0 w=11 h=1
					|  Text\("Hello "\) x=0 y=0 w=6 h=1 DrawBehind
					|  Layout\(\) x=6 y=0 w=5 h=1 DrawBehind
					|
					|OUTPUT:
					|(kotlin\.|java\.lang\.)?UnsupportedOperationException:?
					""".trimMargin().toRegex(),
				)
		}
	}

	@Test fun framesIncludeStatics() = runTest {
		runMosaicTest(withRenderSnapshots = false) {
			setContent {
				Text("Hello")
				Static(snapshotStateListOf("Static")) {
					Text(it)
				}
			}
			assertThat(rendering.render(awaitNodeSnapshot())).isEqualTo(
				"""
				|NODES:
				|Text("Hello") x=0 y=0 w=5 h=1 DrawBehind
				|Static()
				|  Text("Static") x=0 y=0 w=6 h=1 DrawBehind
				|
				|STATIC:
				|Static
				|
				|OUTPUT:
				|Hello
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun framesAfterFirstHaveTimeHeader() = runTest {
		runMosaicTest(withRenderSnapshots = false) {
			setContent {
				var text by remember { mutableStateOf("Hello") }
				Text(text)
				LaunchedEffect(Unit) {
					delay(100L)
					text = "World"
				}
			}

			assertThat(rendering.render(awaitNodeSnapshot())).isEqualTo(
				"""
				|NODES:
				|Text("Hello") x=0 y=0 w=5 h=1 DrawBehind
				|
				|OUTPUT:
				|Hello
				|
				""".trimMargin(),
			)

			timeSource += 100.milliseconds
			assertThat(rendering.render(awaitNodeSnapshot())).isEqualTo(
				"""
				|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ +100ms
				|NODES:
				|Text("World") x=0 y=0 w=5 h=1 DrawBehind
				|
				|OUTPUT:
				|World
				|
				""".trimMargin(),
			)
		}
	}
}
