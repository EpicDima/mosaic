#include "cutils.h"
#include "jni.h"
#include "mosaic.h"
#include <stdlib.h>
#include <string.h>

void throwIse(JNIEnv *env, unsigned int error, const char *prefix) {
	jclass ise = (*env)->FindClass(env, "java/lang/IllegalStateException");

	int prefixLength = strlen(prefix);
	int colonSpaceLength = 2;
	int maxLengthUnsignedDigit = 10;
	int extraNullByte = 1;
	int messageLength = prefixLength + colonSpaceLength + maxLengthUnsignedDigit + extraNullByte;

	char *message = malloc(messageLength * sizeof(char));
	if (message) {
		memcpy(message, prefix, prefixLength);
		message[prefixLength] = ':';
		message[prefixLength + 1] = ' ';
		// Offset the location of the formatted number by the prefix and colon+space lengths.
		sprintf(message + prefixLength + colonSpaceLength, "%lu", error);
		(*env)->ThrowNew(env, ise, message);
	}
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_enterRawMode(JNIEnv *env, jclass type) {
	rawModeResult result = enterRawMode();
	if (likely(!result.error)) {
		return (jlong) result.saved;
	}

	// This throw can fail, but the only condition that should cause that is OOM which
	// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
	throwIse(env, result.error, "Unable to enable raw mode");
	return 0;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_exitRawMode(JNIEnv *env, jclass type, jlong ptr) {
	return exitRawMode((rawModeConfig *) ptr);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_stdinReaderInit(JNIEnv *env, jclass type, jstring path) {
	const char* pathChars = NULL;
	if (path) {
		pathChars = (*env)->GetStringUTFChars(env, path, NULL);
		if (!pathChars) goto err;
	}

	stdinReaderResult result = stdinReader_init(pathChars);

	if (pathChars) {
		(*env)->ReleaseStringUTFChars(env, path, pathChars);
	}

	if (likely(!result.error)) {
		return (jlong) result.reader;
	}

	err:
	// This throw can fail, but the only condition that should cause that is OOM which
	// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
	throwIse(env, result.error, "Unable to create stdin reader");
	return 0;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_stdinReaderRead(
	JNIEnv *env,
	jclass type,
	jlong ptr,
	jbyteArray buffer,
	jint offset,
	jint length
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;

	stdinRead read = stdinReader_read((stdinReader *) ptr, nativeBufferAtOffset, length);

	(*env)->ReleaseByteArrayElements(env, buffer, nativeBuffer, 0);

	if (likely(!read.error)) {
		return read.count;
	}

	// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
	// and should cause the program to try and exit cleanly. 0 is a valid return value.
	throwIse(env, read.error, "Unable to read stdin");
	return -1;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_stdinReaderReadWithTimeout(
	JNIEnv *env,
	jclass type,
	jlong ptr,
	jbyteArray buffer,
	jint offset,
	jint length,
	jint timeoutMillis
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;

	stdinRead read = stdinReader_readWithTimeout(
		(stdinReader *) ptr,
		nativeBufferAtOffset,
		length,
		timeoutMillis
	);

	(*env)->ReleaseByteArrayElements(env, buffer, nativeBuffer, 0);

	if (likely(!read.error)) {
		return read.count;
	}

	// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
	// and should cause the program to try and exit cleanly. 0 is a valid return value.
	throwIse(env, read.error, "Unable to read stdin");
	return -1;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_stdinReaderInterrupt(JNIEnv *env, jclass type, jlong ptr) {
	return stdinReader_interrupt((stdinReader *) ptr);
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_stdinReaderFree(JNIEnv *env, jclass type, jlong ptr) {
	return stdinReader_free((stdinReader *) ptr);
}