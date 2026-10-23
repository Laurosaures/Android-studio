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
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.roundToInt

/** Figma reference: rtyPzQhLf8V3PdCebw5Q7V, six 393 x 852 frames.
 * Native controls retain keyboard, accessibility and touch support.
 * The canvas scales with width and scrolls on shorter displays.
 */
abstract class FigmaScreen : AppCompatActivity() {
    abstract val page: String

    // Elke pagina beschrijft hieronder zijn eigen inhoud.
    abstract fun showPage()
    protected lateinit var canvas: DesignCanvas
    protected val gold = Color.rgb(201, 154, 46)
    protected val ink = Color.rgb(7, 29, 38)
    protected val profileFields = mutableMapOf<String, EditText>()
    protected val maximumAmount = 1000
    protected var amount = 300
    protected val designFont by lazy { Typeface.createFromAsset(assets, "inter.ttf") }
    protected val prefs by lazy { getSharedPreferences("eldorado", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        amount = (savedInstanceState?.getInt("amount") ?: 300).coerceIn(1, maximumAmount)
        val root = FrameLayout(this)
        root.setBackgroundResource(R.drawable.home)
        val scroll = ScrollView(this).apply { isFillViewport = true
        isVerticalScrollBarEnabled = false }
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
        showPage()
    }

    override fun onResume() {
        super.onResume(
            
        )
        // Refresh activities restored from the back stack as well as new screens.
        profileFields.forEach { (key, field) ->
            val saved = prefs.getString(key, "") ?: ""
            if (field.text.toString() != saved) field.setText(saved)
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState
                .putInt("amount", amount)
        super.onSaveInstanceState(outState)
    }

    protected fun navigate(target: Class<out AppCompatActivity>) {
        if (javaClass != target) startActivity(Intent(this, target))
    }

    protected fun box(fill: Int = Color.TRANSPARENT) = GradientDrawable().apply {
        setColor(fill)
        setStroke(resources.displayMetrics.density.roundToInt().coerceAtLeast(1), gold)
        cornerRadius = 2 * resources.displayMetrics.density
    }

    protected fun <T : View> put(view: T, x: Int, y: Int, w: Int, h: Int, size: Float = 0f): T {
        if (view is TextView) view.typeface = designFont
        canvas.addView(view, DesignCanvas.Position(x, (y - 52).coerceAtLeast(0), w, h, size))
        return view
    }

    protected fun text(value: String, x: Int, y: Int, w: Int, h: Int, size: Float = 32f, border: Boolean = false): TextView {
        return put(TextView(this).apply {
            text = value
            setTextColor(Color.rgb(238, 241, 242))
            gravity = Gravity.CENTER
            includeFontPadding = false
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            if (border) background = box()
        }, x, y, w, h, size)
    }

    protected fun button(label: String, x: Int, y: Int, w: Int, h: Int, size: Float = 30f, action: () -> Unit): TextView {
        val v = text(label, x, y, w, h, size)
        v.background = RippleDrawable(ColorStateList.valueOf(0x55D6B654), box(), null)
        v.isFocusable = true
        v.setOnClickListener { action() }
        v.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: android.view.accessibility.AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = "android.widget.Button"
            }
        }
        return v
    }

    protected fun input(hintText: String, key: String, x: Int, y: Int, w: Int, h: Int, email: Boolean = false) =
        put(EditText(this).apply {
            id = View.generateViewId()
            hint = hintText
            setText(prefs.getString(key, ""))
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(130, 145, 151))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            includeFontPadding = false
            setSingleLine(true)
            background = box()
            inputType = if (email) android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS else android.text.InputType.TYPE_CLASS_TEXT
            if (android.os.Build.VERSION.SDK_INT >= 26) setAutofillHints(if (email) View.AUTOFILL_HINT_EMAIL_ADDRESS else View.AUTOFILL_HINT_POSTAL_ADDRESS)
            contentDescription = hintText
            profileFields[key] = this
            doAfterTextChanged { value -> prefs.edit()
                .putString(key, value?.toString().orEmpty()).apply()
            requestLayout() }
        }, x, y, w, h, 18f)

    protected fun header() {
        fun icon(res: Int, label: String, x: Int, w: Int, target: Class<out AppCompatActivity>) {
            put(ImageView(this).apply {
                setImageResource(res)
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = label
                isFocusable = true
                setOnClickListener { navigate(target) }
            }, x, 76, w, 64)
        }
        icon(R.drawable.logo, "Home", 20, 125, home::class.java)
        icon(R.drawable.login, "Login", 176, 117, login::class.java)
        icon(R.drawable.winkelwagen, "Shopping cart", 312, 64, shopingcart::class.java)
        if (page == "game") gameRow(selectedGame, 12, 166, 369, 48, clickable = false)
        else put(EditText(this).apply {
            hint = "Zoek balk"
            setHintTextColor(Color.rgb(238, 241, 242))
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            includeFontPadding = false
            setSingleLine(true)
            background = box()
            contentDescription = "Zoek balk"
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            setOnEditorActionListener { _, _, _ ->
                if (text.isNotBlank()) navigate(game1::class.java)
                false
            }
        }, 12, 166, 369, 48, 32f)
    }

    protected fun title(label: String) {
        text(label, 0, 248, 393, 60, if (label == "Thanks for ordering") 38f else 44f).background = box(Color.rgb(9, 57, 65))
    }

    protected fun tabs(category: Boolean = false) {
        for (i in 0..3) {
            val label = if (category) "Categorie ${i + 1}" else "Thema ${i + 1}"
            val v = button(label, i * 98, 248, if (i == 3) 99 else 98, 60, if (category) 16f else 22f) {
                if (!category) navigate(thema::class.java)
                else Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
            }
            v.background = RippleDrawable(ColorStateList.valueOf(0x66D9B240), box(if (page == "thema" && i == 0) Color.rgb(100, 101, 52) else Color.rgb(9, 57, 65)), null)
        }
    }


    protected val gameNames = arrayOf("Counter-Strike 2", "Dota 2", "Rust", "Apex Legends", "Grand Theft Auto V", "Team Fortress 2", "Baldur’s Gate 3", "Helldivers 2")
    protected val gameImages = intArrayOf(R.drawable.game_cs2, R.drawable.game_dota2, R.drawable.game_rust, R.drawable.game_apex, R.drawable.game_gta5, R.drawable.game_tf2, R.drawable.game_bg3, R.drawable.game_helldivers)
    protected val selectedGame: Int get() = prefs.getInt("selectedGame", 0).coerceIn(0, gameNames.lastIndex)

    protected fun gameRow(index: Int, x: Int, y: Int, w: Int, h: Int, compact: Boolean = false, clickable: Boolean = true) {
        val i = index.coerceIn(0, gameNames.lastIndex)
        val imageWidth = if (compact) 52 else 86
        val row = if (clickable) button("", x, y, w, h) {
            prefs.edit()
                .putInt("selectedGame", i).apply()
            navigate(game1::class.java)
        } else text("", x, y, w, h, border = true)
        row.contentDescription = gameNames[i]
        put(ImageView(this).apply {
            setImageResource(gameImages[i])
            scaleType = ImageView.ScaleType.FIT_CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, x + 5, y + 3, imageWidth, h - 6)
        text(gameNames[i], x + imageWidth + 10, y, w - imageWidth - 15, h, if (compact) 11f else 18f).apply {
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
    }





}

/** Coordinates are design pixels
they are converted into actual screen pixels. */
class DesignCanvas(context: Context) : ViewGroup(context) {
    class Position(val x: Int, val y: Int, val w: Int, val h: Int, val font: Float) : LayoutParams(w, h)
    protected var factor = 1f
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        factor = width / 393f
        setMeasuredDimension(width, maxOf((800 * factor).roundToInt(), if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) MeasureSpec.getSize(heightMeasureSpec) else 0))
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            val p = view.layoutParams as Position
            if (view is TextView && p.font > 0) {
                val normalSize = p.font * factor * resources.configuration.fontScale
                view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, normalSize)
                // Lange invoer verkleinen zodat alles in het veld past.
                if (view is EditText && view.text.isNotEmpty()) {
                    val availableWidth = p.w * factor - view.paddingLeft - view.paddingRight - 8 * factor
                    val textWidth = view.paint.measureText(view.text.toString())
                    if (textWidth > availableWidth) {
                        view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, normalSize * availableWidth / textWidth)
                    }
                }
            }
            view.measure(MeasureSpec.makeMeasureSpec((p.w * factor).roundToInt(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((p.h * factor).roundToInt(), MeasureSpec.EXACTLY))
        }
    }
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            val p = view.layoutParams as Position
            val x = (p.x * factor).roundToInt()
            val y = (p.y * factor).roundToInt()
            view.layout(x, y, x + view.measuredWidth, y + view.measuredHeight)
        }
    }
}
