package com.example.eldorado

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.roundToInt

/** Figma reference: rtyPzQhLf8V3PdCebw5Q7V, six 393 x 852 frames.
 * Native controls retain keyboard, accessibility and touch support.
 * The canvas scales with width and scrolls on shorter displays.
 */
abstract class FigmaScreen : AppCompatActivity() {
    abstract val page: String
    private lateinit var canvas: DesignCanvas
    private val gold = Color.rgb(201, 154, 46)
    private val ink = Color.rgb(7, 29, 38)
    private var amount = 300
    private val designFont by lazy { Typeface.createFromAsset(assets, "inter.ttf") }
    private val prefs by lazy { getSharedPreferences("eldorado", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        amount = savedInstanceState?.getInt("amount") ?: 300
        val root = FrameLayout(this)
        root.setBackgroundResource(R.drawable.home)
        val scroll = ScrollView(this).apply { isFillViewport = true; isVerticalScrollBarEnabled = false }
        canvas = DesignCanvas(this)
        scroll.addView(canvas)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        header()
        when (page) {
            "home", "thema" -> catalog()
            "login" -> loginForm()
            "game" -> game()
            "cart" -> cart()
            "order" -> { title("Thanks for ordering"); button("Back to home", 98, 426, 200, 50) { navigate(home::class.java) } }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("amount", amount)
        super.onSaveInstanceState(outState)
    }

    private fun navigate(target: Class<out AppCompatActivity>) {
        if (javaClass != target) startActivity(Intent(this, target))
    }

    private fun box(fill: Int = Color.TRANSPARENT) = GradientDrawable().apply {
        setColor(fill); setStroke(resources.displayMetrics.density.roundToInt().coerceAtLeast(1), gold)
        cornerRadius = 2 * resources.displayMetrics.density
    }

    private fun <T : View> put(view: T, x: Int, y: Int, w: Int, h: Int, size: Float = 0f): T {
        if (view is TextView) view.typeface = designFont
        canvas.addView(view, DesignCanvas.Position(x, (y - 52).coerceAtLeast(0), w, h, size))
        return view
    }

    private fun text(value: String, x: Int, y: Int, w: Int, h: Int, size: Float = 32f, border: Boolean = false): TextView {
        return put(TextView(this).apply {
            text = value; setTextColor(Color.rgb(238, 241, 242)); gravity = Gravity.CENTER
            includeFontPadding = false; typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            if (border) background = box()
        }, x, y, w, h, size)
    }

    private fun button(label: String, x: Int, y: Int, w: Int, h: Int, size: Float = 30f, action: () -> Unit): TextView {
        val v = text(label, x, y, w, h, size)
        v.background = RippleDrawable(ColorStateList.valueOf(0x55D6B654), box(), null)
        v.isFocusable = true; v.setOnClickListener { action() }
        v.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: android.view.accessibility.AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info); info.className = "android.widget.Button"
            }
        }
        return v
    }

    private fun input(hintText: String, key: String, x: Int, y: Int, w: Int, h: Int, email: Boolean = false) =
        put(EditText(this).apply {
            id = View.generateViewId(); hint = hintText; setText(prefs.getString(key, ""))
            setTextColor(Color.WHITE); setHintTextColor(Color.rgb(130, 145, 151))
            gravity = Gravity.CENTER; setPadding(0, 0, 0, 0); includeFontPadding = false; setSingleLine(true); background = box()
            inputType = if (email) android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS else android.text.InputType.TYPE_CLASS_TEXT
            if (android.os.Build.VERSION.SDK_INT >= 26) setAutofillHints(if (email) View.AUTOFILL_HINT_EMAIL_ADDRESS else View.AUTOFILL_HINT_USERNAME)
            contentDescription = hintText
        }, x, y, w, h, 30f)

    private fun header() {
        fun icon(res: Int, label: String, x: Int, w: Int, target: Class<out AppCompatActivity>) {
            put(ImageView(this).apply {
                setImageResource(res); scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = label; isFocusable = true
                setOnClickListener { navigate(target) }
            }, x, 76, w, 64)
        }
        icon(R.drawable.logo, "Home", 20, 125, home::class.java)
        icon(R.drawable.login, "Login", 176, 117, login::class.java)
        icon(R.drawable.winkelwagen, "Shopping cart", 312, 64, shopingcart::class.java)
        if (page == "game") gameRow(selectedGame, 12, 166, 369, 48, clickable = false)
        else put(EditText(this).apply {
            hint = "Zoek balk"; setHintTextColor(Color.rgb(238, 241, 242)); setTextColor(Color.WHITE)
            gravity = Gravity.CENTER; setPadding(0, 0, 0, 0); includeFontPadding = false; setSingleLine(true); background = box()
            contentDescription = "Zoek balk"; imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            setOnEditorActionListener { _, _, _ ->
                if (text.isNotBlank()) navigate(game1::class.java)
                false
            }
        }, 12, 166, 369, 48, 32f)
    }

    private fun title(label: String) {
        text(label, 0, 248, 393, 60, if (label == "Thanks for ordering") 38f else 44f).background = box(Color.rgb(9, 57, 65))
    }

    private fun tabs(category: Boolean = false) {
        for (i in 0..3) {
            val label = if (category) "Categorie ${i + 1}" else "Thema ${i + 1}"
            val v = button(label, i * 98, 248, if (i == 3) 99 else 98, 60, if (category) 16f else 22f) {
                if (!category) navigate(thema::class.java)
                else Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
            }
            v.background = RippleDrawable(ColorStateList.valueOf(0x66D9B240), box(if (page == "thema" && i == 0) Color.rgb(100, 101, 52) else Color.rgb(9, 57, 65)), null)
        }
    }


    private val gameNames = arrayOf("Counter-Strike 2", "Dota 2", "Rust", "Apex Legends", "Grand Theft Auto V", "Team Fortress 2", "Baldur’s Gate 3", "Helldivers 2")
    private val gameImages = intArrayOf(R.drawable.game_cs2, R.drawable.game_dota2, R.drawable.game_rust, R.drawable.game_apex, R.drawable.game_gta5, R.drawable.game_tf2, R.drawable.game_bg3, R.drawable.game_helldivers)
    private val selectedGame: Int get() = prefs.getInt("selectedGame", 0).coerceIn(0, gameNames.lastIndex)

    private fun gameRow(index: Int, x: Int, y: Int, w: Int, h: Int, compact: Boolean = false, clickable: Boolean = true) {
        val i = index.coerceIn(0, gameNames.lastIndex)
        val imageWidth = if (compact) 52 else 86
        val row = if (clickable) button("", x, y, w, h) {
            prefs.edit().putInt("selectedGame", i).apply()
            navigate(game1::class.java)
        } else text("", x, y, w, h, border = true)
        row.contentDescription = gameNames[i]
        put(ImageView(this).apply {
            setImageResource(gameImages[i]); scaleType = ImageView.ScaleType.FIT_CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, x + 5, y + 3, imageWidth, h - 6)
        text(gameNames[i], x + imageWidth + 10, y, w - imageWidth - 15, h, if (compact) 11f else 18f).apply {
            maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
    }

    private fun catalog() {
        tabs()
        for (i in 0..7) {
            gameRow(i, 24, 330 + i * 66, 345, 48)
        }
    }

    private fun loginForm() {
        title("Login")
        text("", 40, 369, 310, 280, border = true)
        val user = input("Enter username", "username", 57, 387, 278, 60)
        val email = input("Enter E-mail", "email", 57, 479, 278, 60, true)
        button("Login", 122, 567, 149, 50) {
            prefs.edit().putString("username", user.text.toString()).putString("email", email.text.toString()).apply()
            navigate(home::class.java)
        }
    }

    private fun game() {
        tabs(true)
        text("", 43, 338, 309, 246, border = true)
        text("Price € 3/M", 64, 340, 266, 30, 24f)
        val quantity = text("${amount}.M", 149, 372, 99, 70, 30f, true)
        val total = text("Total € ${amount * 3}/${amount}.M", 43, 599, 309, 60, 30f, true)
        fun update(delta: Int) {
            amount = (amount + delta).coerceIn(1, 9999)
            quantity.text = "${amount}.M"; total.text = "Total € ${amount * 3}/${amount}.M"
        }
        button("−", 64, 372, 69, 70, 40f) { update(-1) }
        button("+", 262, 372, 69, 70, 40f) { update(1) }
        text("Hoeveelheid beschikbaar", 64, 479, 265, 38, 20f, true)
        text("Naam verkoper", 64, 534, 265, 40, 30f, true)
        button("Add to cart", 99, 713, 200, 60) {
            prefs.edit().putInt("cartAmount", amount).apply(); navigate(shopingcart::class.java)
        }
    }

    private fun cart() {
        title("Shopping cart")
        text("", 43, 338, 309, 226, border = true)
        val count = prefs.getInt("cartAmount", 300)
        for (i in 0..4) {
            gameRow(i, 56, 346 + i * 44, 169, 30, compact = true, clickable = false)
            text("€ ${count * 3}/${count}.M", 230, 346 + i * 44, 121, 30, 18f)
        }
        text("", 43, 572, 309, 116, border = true)
        val user = input("username", "username", 56, 580, 280, 48)
        val email = input("E-mail", "email", 56, 634, 280, 48, true)
        button("Buy now", 92, 715, 200, 60) {
            prefs.edit().putString("username", user.text.toString()).putString("email", email.text.toString()).apply()
            navigate(order::class.java)
        }
    }
}

/** Coordinates are design pixels; they are converted into actual screen pixels. */
private class DesignCanvas(context: Context) : ViewGroup(context) {
    class Position(val x: Int, val y: Int, val w: Int, val h: Int, val font: Float) : LayoutParams(w, h)
    private var factor = 1f
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        factor = width / 393f
        setMeasuredDimension(width, maxOf((800 * factor).roundToInt(), if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) MeasureSpec.getSize(heightMeasureSpec) else 0))
        for (i in 0 until childCount) {
            val view = getChildAt(i); val p = view.layoutParams as Position
            if (view is TextView && p.font > 0) view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, p.font * factor * resources.configuration.fontScale)
            view.measure(MeasureSpec.makeMeasureSpec((p.w * factor).roundToInt(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((p.h * factor).roundToInt(), MeasureSpec.EXACTLY))
        }
    }
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val view = getChildAt(i); val p = view.layoutParams as Position
            val x = (p.x * factor).roundToInt(); val y = (p.y * factor).roundToInt()
            view.layout(x, y, x + view.measuredWidth, y + view.measuredHeight)
        }
    }
}
