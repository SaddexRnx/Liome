package com.example.engine

import android.os.Build
import android.util.Log

sealed class NativeLibraryState {
    object Loaded : NativeLibraryState()
    data class Unavailable(val reason: String, val architecture: String, val libraryName: String) : NativeLibraryState()
}

/**
 * JNI Bridge to llama.cpp C/C++ native runtime.
 * Declares native bindings and handles dynamic library loading.
 */
object NativeLlamaBridge {
    private const val TAG = "NativeLlamaBridge"
    private const val LIB_NAME = "localmind_llama"

    var libraryState: NativeLibraryState = NativeLibraryState.Unavailable(
        reason = "Uninitialized",
        architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
        libraryName = "lib$LIB_NAME.so"
    )
        private set

    init {
        try {
            System.loadLibrary(LIB_NAME)
            if (!nativeInit()) {
                throw IllegalStateException("llama.cpp backend initialization failed")
            }
            libraryState = NativeLibraryState.Loaded
            Log.i(TAG, "Successfully loaded native library: lib$LIB_NAME.so")
        } catch (e: UnsatisfiedLinkError) {
            val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
            libraryState = NativeLibraryState.Unavailable(
                reason = "Native library 'lib$LIB_NAME.so' not found in APK for ABI $primaryAbi (${e.message}). Ensure native NDK toolchain packages libc++_shared.so and lib$LIB_NAME.so for arm64-v8a.",
                architecture = primaryAbi,
                libraryName = "lib$LIB_NAME.so"
            )
            Log.w(TAG, "Native llama library not present: ${libraryState}")
        } catch (e: Exception) {
            val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
            libraryState = NativeLibraryState.Unavailable(
                reason = "Error loading native library: ${e.localizedMessage}",
                architecture = primaryAbi,
                libraryName = "lib$LIB_NAME.so"
            )
        }
    }

    val isAvailable: Boolean
        get() = libraryState is NativeLibraryState.Loaded

    // Native JNI functions (implemented in C++ llama.cpp wrapper when linked)
    external fun nativeInit(): Boolean
    external fun nativeLoadModel(
        modelPath: String,
        nThreads: Int,
        nContext: Int,
        nGpuLayers: Int,
        useMmap: Boolean,
        useMlock: Boolean
    ): Long

    external fun nativeFreeModel(modelHandle: Long)
    external fun nativeTokenize(modelHandle: Long, text: String): IntArray
    external fun nativeEval(modelHandle: Long, tokens: IntArray, nPast: Int): Boolean
    external fun nativeSampleNextToken(
        modelHandle: Long,
        temperature: Float,
        topP: Float,
        repeatPenalty: Float
    ): Int

    external fun nativeTokenToString(modelHandle: Long, token: Int): String
    external fun nativeGetContextSize(modelHandle: Long): Int
}
