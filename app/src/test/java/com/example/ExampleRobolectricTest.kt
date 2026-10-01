package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.device.DeviceProfiler
import com.example.model.ModelCatalog
import com.example.model.ModelRecommendationEngine
import com.example.model.UserPreferences
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LocalMind", appName)
    }

    @Test
    fun `device profiler extracts valid specs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = DeviceProfiler.profileDevice(context)
        assertTrue(profile.cpuCores >= 1)
        assertTrue(profile.totalRamBytes > 0)
        assertTrue(profile.availableStorageBytes > 0)
    }

    @Test
    fun `model recommendation evaluates catalog models`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = DeviceProfiler.profileDevice(context)
        val prefs = UserPreferences(
            mainIntent = "General assistant",
            priority = "Balanced",
            storageBudget = "1–3 GB",
            answerStyle = "Balanced"
        )

        val firstModel = ModelCatalog.curatedModels.first()
        val rec = ModelRecommendationEngine.evaluate(firstModel, profile, prefs)
        assertNotNull(rec.badgeLabel)
        assertTrue(rec.score in 10..100)
    }

    @Test
    fun `theme presets contain valid color definitions`() {
        assertEquals(4, com.example.ui.theme.ThemePreset.values().size)
    }
}
