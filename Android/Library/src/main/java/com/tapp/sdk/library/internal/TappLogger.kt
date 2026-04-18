package com.tapp.sdk.library.internal

import android.util.Log

private const val LOG_MESSAGE_POINTER = "---> "
private const val FALLBACK_LOG_TAG = "TappSdk"
private const val KOTLIN_FILE_EXTENSION = ".kt"
private const val JAVA_FILE_EXTENSION = ".java"
private const val LOGGER_CLASS_NAME = "TappLoggerKt"

internal fun logD(
    message: String,
    throwable: Throwable? = null
) {
    val callerStackTraceElement = resolveCallerStackTraceElement()
    val tag = resolveCallerTag(callerStackTraceElement)
    val formattedMessage = formatLogMessage(
        callerStackTraceElement = callerStackTraceElement,
        message = message
    )

    if (throwable == null) {
        Log.d(tag, formattedMessage)
    } else {
        Log.d(tag, formattedMessage, throwable)
    }
}

internal fun logI(
    message: String,
    throwable: Throwable? = null
) {
    val callerStackTraceElement = resolveCallerStackTraceElement()
    val tag = resolveCallerTag(callerStackTraceElement)
    val formattedMessage = formatLogMessage(
        callerStackTraceElement = callerStackTraceElement,
        message = message
    )

    if (throwable == null) {
        Log.i(tag, formattedMessage)
    } else {
        Log.i(tag, formattedMessage, throwable)
    }
}

internal fun logV(
    message: String,
    throwable: Throwable? = null
) {
    val callerStackTraceElement = resolveCallerStackTraceElement()
    val tag = resolveCallerTag(callerStackTraceElement)
    val formattedMessage = formatLogMessage(
        callerStackTraceElement = callerStackTraceElement,
        message = message
    )

    if (throwable == null) {
        Log.v(tag, formattedMessage)
    } else {
        Log.v(tag, formattedMessage, throwable)
    }
}

private fun formatLogMessage(
    callerStackTraceElement: StackTraceElement?,
    message: String
): String {
    val sourceLocation = callerStackTraceElement?.toSourceLocation()

    return if (sourceLocation == null) {
        "$LOG_MESSAGE_POINTER$message"
    } else {
        "$sourceLocation $LOG_MESSAGE_POINTER$message"
    }
}

private fun resolveCallerStackTraceElement(): StackTraceElement? {
    return Thread.currentThread().stackTrace.firstOrNull { stackTraceElement ->
        stackTraceElement.className.isCallerClassName()
    }
}

private fun resolveCallerTag(callerStackTraceElement: StackTraceElement?): String {
    return callerStackTraceElement?.fileName
        ?.removeSuffix(KOTLIN_FILE_EXTENSION)
        ?.removeSuffix(JAVA_FILE_EXTENSION)
        ?: callerStackTraceElement?.className
            ?.substringAfterLast('.')
            ?.substringBefore('$')
        ?: FALLBACK_LOG_TAG
}

private fun StackTraceElement.toSourceLocation(): String? {
    val sourceFileName = fileName ?: return null

    if (lineNumber <= 0) {
        return null
    }

    return "$sourceFileName:$lineNumber"
}

private fun String.isCallerClassName(): Boolean {
    return this != Thread::class.java.name &&
        !endsWith(LOGGER_CLASS_NAME) &&
        !startsWith("dalvik.") &&
        !startsWith("java.lang.reflect.")
}
