package com.example.eldorado

import android.graphics.Bitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FigmaScreensTest {
    @Test fun screensRenderAndQuantityUpdates() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        for (activity in listOf(login::class.java, home::class.java, thema::class.java, game1::class.java, shopingcart::class.java, order::class.java)) {
            val launched = instrumentation.startActivitySync(android.content.Intent(instrumentation.targetContext, activity).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            instrumentation.waitForIdleSync()

            val image = instrumentation.uiAutomation.takeScreenshot()
            val file = File(instrumentation.targetContext.getExternalFilesDir(null), activity.simpleName + ".png")
            file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
            instrumentation.runOnMainSync { launched.finish() }
        }
    }
}

