package com.rocasspb.avaawaand.utils

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
external fun dateNow(): Double

actual object PlatformUtils {
    actual fun currentTimeMillis(): Long = dateNow().toLong()
}
