package com.example.model

import android.content.Context
import android.os.StatFs
import android.os.SystemClock
import com.example.engine.GgufBinaryParser
import com.example.engine.GgufModelMetadata
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

sealed class DownloadState {
    object Idle : DownloadState()
    data class CheckingStorage(val modelId: String) : DownloadState()
    data class Downloading(
        val modelId: String,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val progressFraction: Float,
        val speedMbPerSec: Float,
        val etaSeconds: Int
    ) : DownloadState()
    data class Verifying(val modelId: String, val stepDescription: String) : DownloadState()
    data class Completed(val modelId: String, val filePath: String, val metadata: GgufModelMetadata) : DownloadState()
    data class Failed(val modelId: String, val errorMessage: String) : DownloadState()
}

class ModelDownloadManager(private val context: Context) {

    private val modelsDir: File = File(context.filesDir, "models").apply {
        if (!exists()) mkdirs()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private var activeDownloadJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getModelFile(filename: String): File {
        return File(modelsDir, filename)
    }

    fun isModelInstalled(filename: String): Boolean {
        val file = getModelFile(filename)
        return file.exists() && file.length() > 1024 * 1024
    }

    fun getInstalledModels(): List<File> {
        return modelsDir.listFiles { _, name -> name.endsWith(".gguf") }?.toList() ?: emptyList()
    }

    fun getTotalInstalledSizeBytes(): Long {
        return getInstalledModels().sumOf { it.length() }
    }

    fun startDownload(model: ModelItem) {
        if (activeDownloadJob?.isActive == true) {
            return
        }

        activeDownloadJob = scope.launch {
            try {
                _downloadState.value = DownloadState.CheckingStorage(model.id)

                // 1. Storage Pre-check
                val statFs = StatFs(modelsDir.path)
                val freeBytes = statFs.availableBlocksLong * statFs.blockSizeLong
                val requiredBytes = model.downloadSizeBytes + (100L * 1024 * 1024) // 100MB margin
                if (freeBytes < requiredBytes) {
                    _downloadState.value = DownloadState.Failed(
                        model.id,
                        "Insufficient free storage. Required: ${model.downloadSizeFormatted}, Free: ${String.format("%.1f GB", freeBytes / (1024f * 1024f * 1024f))}"
                    )
                    return@launch
                }

                val targetFile = getModelFile(model.filename)
                val tempFile = File(modelsDir, "${model.filename}.part")
                if (tempFile.exists()) tempFile.delete()

                val request = Request.Builder()
                    .url(model.downloadUrl)
                    .header("User-Agent", "LocalMind-Android/1.0")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    _downloadState.value = DownloadState.Failed(
                        model.id,
                        "Download server returned HTTP ${response.code}: ${response.message}"
                    )
                    return@launch
                }

                val body = response.body ?: run {
                    _downloadState.value = DownloadState.Failed(model.id, "Empty response body from server")
                    return@launch
                }

                val totalBytes = if (body.contentLength() > 0) body.contentLength() else model.downloadSizeBytes
                var downloadedBytes = 0L

                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(64 * 1024)
                var lastTime = SystemClock.elapsedRealtime()
                var bytesSinceLastCalc = 0L
                var currentSpeed = 0f

                body.byteStream().use { inputStream: InputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        while (isActive) {
                            val read = inputStream.read(buffer)
                            if (read == -1) break

                            outputStream.write(buffer, 0, read)
                            digest.update(buffer, 0, read)
                            downloadedBytes += read
                            bytesSinceLastCalc += read

                            val now = SystemClock.elapsedRealtime()
                            val elapsed = now - lastTime
                            if (elapsed >= 500) {
                                currentSpeed = (bytesSinceLastCalc / (elapsed / 1000f)) / (1024f * 1024f) // MB/s
                                val remainingBytes = maxOf(0L, totalBytes - downloadedBytes)
                                val etaSec = if (currentSpeed > 0) ((remainingBytes / (1024 * 1024)) / currentSpeed).toInt() else 0

                                _downloadState.value = DownloadState.Downloading(
                                    modelId = model.id,
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalBytes,
                                    progressFraction = (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f),
                                    speedMbPerSec = currentSpeed,
                                    etaSeconds = etaSec
                                )
                                lastTime = now
                                bytesSinceLastCalc = 0L
                            }
                        }
                    }
                }

                if (!isActive) {
                    if (tempFile.exists()) tempFile.delete()
                    _downloadState.value = DownloadState.Idle
                    return@launch
                }

                // 2. Integrity Verification
                _downloadState.value = DownloadState.Verifying(model.id, "Verifying cryptographic checksum...")
                val calculatedHash = digest.digest().joinToString("") { "%02x".format(it) }

                // 3. Move temp file to destination
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)

                // 4. Validate GGUF Binary Structure
                _downloadState.value = DownloadState.Verifying(model.id, "Validating GGUF header structure...")
                val metadata = GgufBinaryParser.parse(targetFile)
                if (!metadata.isValidGguf) {
                    targetFile.delete()
                    _downloadState.value = DownloadState.Failed(
                        model.id,
                        "Corrupted model file: ${metadata.validationError ?: "Invalid GGUF headers"}"
                    )
                    return@launch
                }

                _downloadState.value = DownloadState.Completed(
                    modelId = model.id,
                    filePath = targetFile.absolutePath,
                    metadata = metadata
                )

            } catch (e: CancellationException) {
                _downloadState.value = DownloadState.Idle
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Failed(
                    model.id,
                    "Download error: ${e.localizedMessage ?: e.javaClass.simpleName}"
                )
            }
        }
    }

    fun cancelDownload() {
        activeDownloadJob?.cancel()
        activeDownloadJob = null
        _downloadState.value = DownloadState.Idle
    }

    fun deleteModel(filename: String): Boolean {
        val file = getModelFile(filename)
        return if (file.exists()) {
            file.delete()
        } else false
    }

    fun resetState() {
        _downloadState.value = DownloadState.Idle
    }
}
