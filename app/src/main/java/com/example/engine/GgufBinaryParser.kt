package com.example.engine

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

data class GgufModelMetadata(
    val architecture: String,
    val modelName: String,
    val contextLength: Int,
    val embeddingLength: Int,
    val blockCount: Int,
    val headCount: Int,
    val feedForwardLength: Int,
    val quantizationType: String,
    val tensorCount: Long,
    val fileSizeBytes: Long,
    val isValidGguf: Boolean,
    val validationError: String? = null
)

object GgufBinaryParser {

    private const val GGUF_MAGIC = 0x46554747 // "GGUF" in little endian

    // Value types in GGUF
    private const val TYPE_UINT8 = 0
    private const val TYPE_INT8 = 1
    private const val TYPE_UINT16 = 2
    private const val TYPE_INT16 = 3
    private const val TYPE_UINT32 = 4
    private const val TYPE_INT32 = 5
    private const val TYPE_FLOAT32 = 6
    private const val TYPE_BOOL = 7
    private const val TYPE_STRING = 8
    private const val TYPE_ARRAY = 9
    private const val TYPE_UINT64 = 10
    private const val TYPE_INT64 = 11
    private const val TYPE_FLOAT64 = 12

    fun parse(file: File): GgufModelMetadata {
        if (!file.exists() || !file.canRead()) {
            return GgufModelMetadata(
                architecture = "unknown",
                modelName = file.name,
                contextLength = 0,
                embeddingLength = 0,
                blockCount = 0,
                headCount = 0,
                feedForwardLength = 0,
                quantizationType = "Unknown",
                tensorCount = 0,
                fileSizeBytes = if (file.exists()) file.length() else 0,
                isValidGguf = false,
                validationError = "File does not exist or cannot be read"
            )
        }

        val fileSize = file.length()
        if (fileSize < 32) {
            return GgufModelMetadata(
                architecture = "unknown",
                modelName = file.name,
                contextLength = 0,
                embeddingLength = 0,
                blockCount = 0,
                headCount = 0,
                feedForwardLength = 0,
                quantizationType = "Corrupted",
                tensorCount = 0,
                fileSizeBytes = fileSize,
                isValidGguf = false,
                validationError = "File size is too small to be a valid GGUF model ($fileSize bytes)"
            )
        }

        try {
            RandomAccessFile(file, "r").use { raf ->
                val channel = raf.channel
                // Read first 2MB to parse metadata headers
                val headerSize = minOf(fileSize, 2L * 1024 * 1024).toInt()
                val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, headerSize.toLong())
                buffer.order(ByteOrder.LITTLE_ENDIAN)

                val magic = buffer.int
                if (magic != GGUF_MAGIC) {
                    return GgufModelMetadata(
                        architecture = "unknown",
                        modelName = file.name,
                        contextLength = 0,
                        embeddingLength = 0,
                        blockCount = 0,
                        headCount = 0,
                        feedForwardLength = 0,
                        quantizationType = "Invalid Magic",
                        tensorCount = 0,
                        fileSizeBytes = fileSize,
                        isValidGguf = false,
                        validationError = "Invalid GGUF header magic: expected 'GGUF', found 0x${Integer.toHexString(magic)}"
                    )
                }

                val version = buffer.int
                if (version != 2 && version != 3) {
                    return GgufModelMetadata(
                        architecture = "unknown",
                        modelName = file.name,
                        contextLength = 0,
                        embeddingLength = 0,
                        blockCount = 0,
                        headCount = 0,
                        feedForwardLength = 0,
                        quantizationType = "Unsupported Version",
                        tensorCount = 0,
                        fileSizeBytes = fileSize,
                        isValidGguf = false,
                        validationError = "Unsupported GGUF version: $version (supports v2 & v3)"
                    )
                }

                val tensorCount = buffer.long
                val metadataKvCount = buffer.long

                var architecture = "llama"
                var modelName = file.nameWithoutExtension
                var contextLength = 2048
                var embeddingLength = 0
                var blockCount = 0
                var headCount = 0
                var feedForwardLength = 0
                var fileTypeInt = -1

                // Parse metadata key-value pairs
                val pairsToRead = minOf(metadataKvCount, 300L).toInt()
                for (i in 0 until pairsToRead) {
                    if (buffer.remaining() < 12) break
                    val key = readGgufString(buffer) ?: break
                    if (buffer.remaining() < 4) break
                    val valType = buffer.int

                    when (valType) {
                        TYPE_UINT8, TYPE_INT8 -> {
                            if (buffer.remaining() >= 1) buffer.get()
                        }
                        TYPE_UINT16, TYPE_INT16 -> {
                            if (buffer.remaining() >= 2) buffer.short
                        }
                        TYPE_UINT32, TYPE_INT32 -> {
                            if (buffer.remaining() >= 4) {
                                val intVal = buffer.int
                                when {
                                    key.endsWith(".context_length") -> contextLength = intVal
                                    key.endsWith(".embedding_length") -> embeddingLength = intVal
                                    key.endsWith(".block_count") -> blockCount = intVal
                                    key.endsWith(".feed_forward_length") -> feedForwardLength = intVal
                                    key.endsWith(".head_count") || key.endsWith(".attention.head_count") -> headCount = intVal
                                    key == "general.file_type" -> fileTypeInt = intVal
                                }
                            }
                        }
                        TYPE_FLOAT32 -> {
                            if (buffer.remaining() >= 4) buffer.float
                        }
                        TYPE_BOOL -> {
                            if (buffer.remaining() >= 1) buffer.get()
                        }
                        TYPE_STRING -> {
                            val strVal = readGgufString(buffer)
                            if (strVal != null) {
                                when (key) {
                                    "general.architecture" -> architecture = strVal
                                    "general.name" -> modelName = strVal
                                }
                            }
                        }
                        TYPE_ARRAY -> {
                            skipGgufArray(buffer)
                        }
                        TYPE_UINT64, TYPE_INT64 -> {
                            if (buffer.remaining() >= 8) {
                                val longVal = buffer.long
                                when {
                                    key.endsWith(".context_length") -> contextLength = longVal.toInt()
                                    key.endsWith(".embedding_length") -> embeddingLength = longVal.toInt()
                                    key.endsWith(".block_count") -> blockCount = longVal.toInt()
                                    key.endsWith(".feed_forward_length") -> feedForwardLength = longVal.toInt()
                                    key.endsWith(".head_count") || key.endsWith(".attention.head_count") -> headCount = longVal.toInt()
                                }
                            }
                        }
                        TYPE_FLOAT64 -> {
                            if (buffer.remaining() >= 8) buffer.double
                        }
                        else -> {
                            // Unknown type, abort metadata iteration
                            break
                        }
                    }
                }

                val quantType = formatQuantization(fileTypeInt, file.name)

                return GgufModelMetadata(
                    architecture = architecture,
                    modelName = modelName,
                    contextLength = if (contextLength > 0) contextLength else 2048,
                    embeddingLength = embeddingLength,
                    blockCount = blockCount,
                    headCount = headCount,
                    feedForwardLength = feedForwardLength,
                    quantizationType = quantType,
                    tensorCount = tensorCount,
                    fileSizeBytes = fileSize,
                    isValidGguf = true,
                    validationError = null
                )
            }
        } catch (e: Exception) {
            return GgufModelMetadata(
                architecture = "unknown",
                modelName = file.name,
                contextLength = 0,
                embeddingLength = 0,
                blockCount = 0,
                headCount = 0,
                feedForwardLength = 0,
                quantizationType = "Error",
                tensorCount = 0,
                fileSizeBytes = fileSize,
                isValidGguf = false,
                validationError = "Parsing exception: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    private fun readGgufString(buffer: ByteBuffer): String? {
        if (buffer.remaining() < 8) return null
        val length = buffer.long
        if (length < 0 || length > 100_000 || length > buffer.remaining()) {
            return null
        }
        val bytes = ByteArray(length.toInt())
        buffer.get(bytes)
        return String(bytes, Charsets.UTF_8)
    }

    private fun skipGgufArray(buffer: ByteBuffer) {
        if (buffer.remaining() < 12) return
        val itemType = buffer.int
        val itemCount = buffer.long
        if (itemCount < 0 || itemCount > 10_000) return

        when (itemType) {
            TYPE_UINT8, TYPE_INT8, TYPE_BOOL -> {
                val skip = minOf(itemCount, buffer.remaining().toLong()).toInt()
                buffer.position(buffer.position() + skip)
            }
            TYPE_UINT16, TYPE_INT16 -> {
                val skip = minOf(itemCount * 2, buffer.remaining().toLong()).toInt()
                buffer.position(buffer.position() + skip)
            }
            TYPE_UINT32, TYPE_INT32, TYPE_FLOAT32 -> {
                val skip = minOf(itemCount * 4, buffer.remaining().toLong()).toInt()
                buffer.position(buffer.position() + skip)
            }
            TYPE_UINT64, TYPE_INT64, TYPE_FLOAT64 -> {
                val skip = minOf(itemCount * 8, buffer.remaining().toLong()).toInt()
                buffer.position(buffer.position() + skip)
            }
            TYPE_STRING -> {
                for (i in 0 until minOf(itemCount, 1000L).toInt()) {
                    readGgufString(buffer) ?: break
                }
            }
            else -> {
                // Nested or unknown, skip what we can
            }
        }
    }

    private fun formatQuantization(fileType: Int, filename: String): String {
        val lower = filename.lowercase()
        return when {
            lower.contains("q4_k_m") -> "Q4_K_M (4-bit medium)"
            lower.contains("q4_k_s") -> "Q4_K_S (4-bit small)"
            lower.contains("q4_0") -> "Q4_0 (4-bit legacy)"
            lower.contains("q5_k_m") -> "Q5_K_M (5-bit medium)"
            lower.contains("q8_0") -> "Q8_0 (8-bit high)"
            lower.contains("f16") -> "F16 (16-bit float)"
            fileType == 2 -> "Q4_0"
            fileType == 3 -> "Q4_1"
            fileType == 7 -> "Q8_0"
            fileType == 12 -> "Q4_K_M"
            fileType == 15 -> "Q5_K_M"
            else -> "Q4_K_M (Quantized)"
        }
    }
}
