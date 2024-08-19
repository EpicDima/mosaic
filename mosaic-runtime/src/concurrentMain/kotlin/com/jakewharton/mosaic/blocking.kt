package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import kotlinx.coroutines.runBlocking

public fun runMosaicBlocking(content: @Composable () -> Unit) {
	runBlocking {
		runMosaic(content)
	}
}

public fun renderMosaicBlocking(content: @Composable () -> Unit): String {
	return runBlocking {
		renderMosaic(content)
	}
}
