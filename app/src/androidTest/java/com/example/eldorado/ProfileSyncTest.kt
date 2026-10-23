package com.example.eldorado

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileSyncTest {
    @Test fun loginDetailsReachCartWithoutPressingLogin() {
        val ins = InstrumentationRegistry.getInstrumentation()
        val prefs = ins.targetContext.getSharedPreferences("eldorado", 0)
        val original = listOf("address", "email").associateWith { prefs.getString(it, null) }
        val opened = mutableListOf<Activity>()
        fun find(view: View, label: String): View? {
            if (view.contentDescription?.toString() == label) return view
            if (view is ViewGroup) for (i in 0 until view.childCount) {
                val result = find(view.getChildAt(i), label)
                if (result != null) return result
            }
            return null
        }
        try {
            val loginActivity = ins.startActivitySync(Intent(ins.targetContext, login::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            opened.add(loginActivity)
            ins.waitForIdleSync()
            val monitor = ins.addMonitor(shopingcart::class.java.name, null, false)
            ins.runOnMainSync {
                (find(loginActivity.window.decorView, "Address") as EditText).setText("Profile test")
                (find(loginActivity.window.decorView, "Enter E-mail") as EditText).setText("profile@example.com")
                find(loginActivity.window.decorView, "Shopping cart")!!.performClick()
            }
            val cart = ins.waitForMonitorWithTimeout(monitor, 5000)
            ins.removeMonitor(monitor)
            assertNotNull(cart)
            opened.add(cart)
            ins.waitForIdleSync()
            ins.runOnMainSync {
                assertEquals("Profile test", (find(cart.window.decorView, "Address") as EditText).text.toString())
                assertEquals("profile@example.com", (find(cart.window.decorView, "E-mail") as EditText).text.toString())
                (find(cart.window.decorView, "Address") as EditText).setText("Updated profile")
                (find(cart.window.decorView, "E-mail") as EditText).setText("")
                cart.finish()
            }
            ins.waitForIdleSync()
            ins.runOnMainSync {
                assertEquals("Updated profile", (find(loginActivity.window.decorView, "Address") as EditText).text.toString())
                assertEquals("", (find(loginActivity.window.decorView, "Enter E-mail") as EditText).text.toString())
            }
        } finally {
            ins.runOnMainSync { opened.reversed().forEach { if (!it.isFinishing) it.finish() } }
            val edit = prefs.edit()
            original.forEach { (key, value) -> if (value == null) edit.remove(key) else edit.putString(key, value) }
            edit.commit()
        }
    }
}

