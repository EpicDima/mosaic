package com.jakewharton.mosaic.layout

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.TestChar
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicTest
import com.jakewharton.mosaic.s
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.unit.IntOffset
import kotlin.test.Test
import kotlin.test.assertFails
import kotlinx.coroutines.test.runTest

class OffsetTest {
	@Test fun offsetHorizontalFixed() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(3, 0)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|   $TestChar $s
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetHorizontalFixedBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(30, 0)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetHorizontalFixedNegativeBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(-3, 0)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetVerticalFixed() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(0, 4)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|$TestChar    $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetVerticalFixedBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(0, 40)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetVerticalFixedNegativeBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(0, -4)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetFixed() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(3, 4)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|   $TestChar $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetFixedBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(30, 40)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetFixedNegativeBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(-3, -4)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetFixedDebug() {
		val actual = Modifier.offset(3, 4).toString()
		assertThat(actual).isEqualTo("Offset(x=3, y=4)")
	}

	@Test fun offsetHorizontalModifiable() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(3, 0) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|   $TestChar $s
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetHorizontalModifiableBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(30, 0) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitNodeSnapshot()
			}
		}
	}

	@Test fun offsetHorizontalModifiableNegativeBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(-3, 0) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetVerticalModifiable() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(0, 4) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|$TestChar    $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetVerticalModifiableBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(0, 40) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetVerticalModifiableNegativeBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(0, -4) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetModifiable() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(3, 4) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|   $TestChar $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetModifiableBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(30, 40) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetModifiableNegativeBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(-3, -4) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertFails {
				awaitRenderSnapshot()
			}
		}
	}

	@Test fun offsetModifiableDebug() {
		val offsetLambda = { IntOffset(-3, -4) }
		val actual = Modifier.offset(offsetLambda).toString()
		assertThat(actual).isEqualTo("ChangeableOffset(offset=$offsetLambda)")
	}
}
