package com.jakewharton.mosaic

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class MosaicTest {
	@Test fun renderMosaicSimple() {
		val actual = renderMosaic {
			Column {
				Text("One")
				Text("Two")
				Text("Three")
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|${ansiMoveCursorToFirstColumn}One  $ansiClearLineAfterCursor
			|Two  $ansiClearLineAfterCursor
			|Three$ansiClearLineAfterCursor$ansiClearAllAfterCursor
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun renderMosaicIgnoreLaunchedEffect() {
		val actual = renderMosaic {
			var count by remember { mutableIntStateOf(0) }

			Column {
				Text("One")
				Text("Two")
				Text("Three")
				repeat(count) {
					Text("Any number")
				}
			}

			LaunchedEffect(Unit) {
				while (true) {
					count++
					delay(50L)
				}
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|${ansiMoveCursorToFirstColumn}One  $ansiClearLineAfterCursor
			|Two  $ansiClearLineAfterCursor
			|Three$ansiClearLineAfterCursor$ansiClearAllAfterCursor
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun renderMosaicIgnoreDisposableEffect() {
		val actual = renderMosaic {
			var count by remember { mutableIntStateOf(0) }

			Column {
				Text("One")
				Text("Two")
				Text("Three")
				repeat(count) {
					Text("Any number")
				}
			}

			DisposableEffect(Unit) {
				count++
				onDispose {
					count++
				}
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|${ansiMoveCursorToFirstColumn}One  $ansiClearLineAfterCursor
			|Two  $ansiClearLineAfterCursor
			|Three$ansiClearLineAfterCursor$ansiClearAllAfterCursor
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun renderMosaicIgnoreMultipleEffects() {
		val actual = renderMosaic {
			var count by remember { mutableIntStateOf(0) }

			DisposableEffect(Unit) {
				count = 1
				onDispose {
					count = 2
				}
			}

			LaunchedEffect(Unit) {
				count = 3
			}

			SideEffect {
				count = 4
			}

			Column {
				Text("One")
				Text("Two")
				Text("Three")
				repeat(count) {
					Text("Any number")
				}
			}

			LaunchedEffect(Unit) {
				count = 5
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|${ansiMoveCursorToFirstColumn}One  $ansiClearLineAfterCursor
			|Two  $ansiClearLineAfterCursor
			|Three$ansiClearLineAfterCursor$ansiClearAllAfterCursor
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun renderMosaicInDefaultCoroutineDispatcher() = runTest {
		val actual = withContext(Dispatchers.Default) {
			renderMosaic {
				Column {
					Text("One")
					Text("Two")
					Text("Three")
				}
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|${ansiMoveCursorToFirstColumn}One  $ansiClearLineAfterCursor
			|Two  $ansiClearLineAfterCursor
			|Three$ansiClearLineAfterCursor$ansiClearAllAfterCursor
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun renderMosaicConcurrently() = runTest {
		val actuals = List(100) {
			async(Dispatchers.Default, start = CoroutineStart.LAZY) {
				renderMosaic {
					Column {
						Text("One")
						Text("Two")
						Text("Three")
					}
				}
			}
		}.awaitAll()

		actuals.forEach { actual ->
			assertThat(actual).isEqualTo(
				"""
				|${ansiMoveCursorToFirstColumn}One  $ansiClearLineAfterCursor
				|Two  $ansiClearLineAfterCursor
				|Three$ansiClearLineAfterCursor$ansiClearAllAfterCursor
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}
}
