package com.example.eldorado

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StockLimitTest {
    @Test fun quantityStaysBetweenOneAndOneThousand() {
        val ins = InstrumentationRegistry.getInstrumentation()
        val activity = ins.startActivitySync(Intent(ins.targetContext, game1::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        fun find(view: View, label: String): TextView? {
            if (view is TextView && view.text.toString() == label) return view
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val found = find(view.getChildAt(i), label)
                    if (found != null) return found
                }
            }
            return null
        }
        try {
            ins.waitForIdleSync()
            ins.runOnMainSync {
                val root = activity.window.decorView
                val plus = find(root, "+")!!
                val minus = find(root, "−")!!
                repeat(1100) { plus.performClick() }
                assertNotNull(find(root, "1000.M"))
                assertNotNull(find(root, "Total € 3000/1000.M"))
                repeat(1100) { minus.performClick() }
                assertNotNull(find(root, "1.M"))
            }
        } finally {
            ins.runOnMainSync { activity.finish() }
        }
    }
}

