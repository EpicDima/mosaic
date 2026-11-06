# Module mosaic-runtime

Core of Mosaic which runs a Compose composition and renders it to the terminal.

A program starts with one of the `runMosaic` functions:

- `runMosaic` suspends until the composition completes.
- `runMosaicBlocking` blocks the calling thread until the composition completes.
- `runMosaicMain` is a shorthand for `runMosaicBlocking` with the default policy for use as `fun main()`.

The composition completes once all of its effects finish and there is nothing left to recompose.

```kotlin
fun main() = runMosaicMain {
  var count by remember { mutableIntStateOf(0) }

  Text("The count is: $count")

  LaunchedEffect(Unit) {
    for (i in 1..20) {
      delay(250.milliseconds)
      count = i
    }
  }
}
```

By default, the process exits with an error when there is no interactive terminal (for example,
on CI or when running through Gradle or an IDE).
Pass a different `NonInteractivePolicy` to `runMosaic` or `runMosaicBlocking` to change this.

The UI is built from composables such as `Text`, `Row`, `Column`, `Box`, and `Spacer`, and from
modifiers such as `padding`, `size`, `background`, and `onKeyEvent`.
The current terminal size, focus, and theme are available through `LocalTerminalState`.
