# Module mosaic-testing

Utilities for testing Mosaic composables without a real terminal.

`runMosaicTest` runs a composition against a fake terminal with a test frame clock.
Each call to `awaitSnapshot` advances to the next frame with changes and returns its rendered
output, or fails if there are no changes within a timeout.
Use `setContentAndSnapshot` to capture the initial frame right away.

Combine it with `runTest` from `kotlinx-coroutines-test` to skip delays using virtual time.

This tests the `Counter` composable from the
[counter sample](https://github.com/JakeWharton/mosaic/tree/trunk/samples/counter):

```kotlin
@Test fun counter() = runTest {
  runMosaicTest {
    setContent {
      Counter()
    }
    for (count in 0..20) {
      assertEquals("The count is: $count", awaitSnapshot())
    }
  }
}
```

By default, snapshots are plain text without ANSI escape sequences.
Pass a different `SnapshotStrategy` to `runMosaicTest` to receive snapshots in another form,
such as `MosaicSnapshots` for the underlying `Mosaic` instance.

Keyboard input can be simulated with `sendKeyEvent`, and the fake terminal's size, focus, and
theme can be changed through `state`.
