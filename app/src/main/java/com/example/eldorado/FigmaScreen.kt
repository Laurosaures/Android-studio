package com.example.eldorado

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import kotlin.math.roundToInt

/**
 * Basis van alle schermen. Het Figma-ontwerp is 393 x 852 pixels.
 * Alle coordinaten in deze app zijn ontwerppixels. DesignCanvas rekent ze
 * om naar de echte schermgrootte. Op korte schermen kun je scrollen.
 */
abstract class FigmaScreen : AppCompatActivity() {

    // Elk scherm vult deze twee dingen zelf in
    abstract val page: String
    abstract fun showPage()

    // Kleuren
    protected val gold = Color.rgb(201, 154, 46)
    protected val ink = Color.rgb(7, 29, 38)

    // Gegevens
    protected lateinit var canvas: DesignCanvas
    protected val profileFields = mutableMapOf<String, EditText>()
    protected val maximumAmount = 1000
    protected var amount = 300

    // Lettertype en opslag (worden pas gemaakt als ze nodig zijn)
    protected val designFont by lazy { Typeface.createFromAsset(assets, "inter.ttf") }
    protected val prefs by lazy { getSharedPreferences("eldorado", MODE_PRIVATE) }

    // Lijst met games
    protected val gameNames = arrayOf(
        "Counter-Strike 2", "Dota 2", "Rust", "Apex Legends",
        "Grand Theft Auto V", "Team Fortress 2", "Baldur’s Gate 3", "Helldivers 2"
    )
    protected val gameImages = intArrayOf(
        R.drawable.game_cs2, R.drawable.game_dota2, R.drawable.game_rust,
        R.drawable.game_apex, R.drawable.game_gta5, R.drawable.game_tf2,
        R.drawable.game_bg3, R.drawable.game_helldivers
    )

    // De gekozen game, altijd binnen de lijst
    protected val selectedGame: Int
        get() = prefs.getInt("selectedGame", 0).coerceIn(0, gameNames.lastIndex)

    // ---------------------------------------------------------------
    // Levenscyclus van het scherm
    // ---------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Lichte iconen in de statusbalk (achtergrond is donker)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false

        // Hoeveelheid terugzetten na bijvoorbeeld draaien van het scherm
        val savedAmount = savedInstanceState?.getInt("amount") ?: 300
        amount = savedAmount.coerceIn(1, maximumAmount)

        // Opbouw: root -> scrollview -> canvas
        val root = FrameLayout(this)
        root.setBackgroundResource(R.drawable.home)

        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        scroll.isVerticalScrollBarEnabled = false

        canvas = DesignCanvas(this)
        scroll.addView(canvas)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)

        // Ruimte laten voor statusbalk, navigatiebalk en toetsenbord
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val types = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            val bars = insets.getInsets(types)
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        header()
        showPage()
    }

    override fun onResume() {
        super.onResume()
        // Velden bijwerken als de opgeslagen waarde anders is (bijv. na terug-knop)
        for ((key, field) in profileFields) {
            val saved = prefs.getString(key, "") ?: ""
            if (field.text.toString() != saved) {
                field.setText(saved)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("amount", amount)
        super.onSaveInstanceState(outState)
    }

    // ---------------------------------------------------------------
    // Hulpfuncties
    // ---------------------------------------------------------------

    // Ga naar een ander scherm, maar niet naar jezelf
    protected fun navigate(target: Class<out AppCompatActivity>) {
        if (javaClass != target) {
            startActivity(Intent(this, target))
        }
    }

    // Vakje met gouden rand en licht afgeronde hoeken
    protected fun box(fill: Int = Color.TRANSPARENT): GradientDrawable {
        val density = resources.displayMetrics.density
        val drawable = GradientDrawable()
        drawable.setColor(fill)
        drawable.setStroke(density.roundToInt().coerceAtLeast(1), gold)
        drawable.cornerRadius = 2 * density
        return drawable
    }

    // Zet een view op het canvas (x, y, w, h in ontwerppixels)
    protected fun <T : View> put(view: T, x: Int, y: Int, w: Int, h: Int, size: Float = 0f): T {
        if (view is TextView) {
            view.typeface = designFont
        }
        // 52 eraf, want de statusbalk zit al in de padding
        val newY = (y - 52).coerceAtLeast(0)
        canvas.addView(view, DesignCanvas.Position(x, newY, w, h, size))
        return view
    }

    // Gecentreerde tekst, eventueel met rand
    protected fun text(
        value: String, x: Int, y: Int, w: Int, h: Int,
        size: Float = 32f, border: Boolean = false
    ): TextView {
        val tv = TextView(this)
        tv.text = value
        tv.setTextColor(Color.rgb(238, 241, 242))
        tv.gravity = Gravity.CENTER
        tv.includeFontPadding = false
        if (border) {
            tv.background = box()
        }
        return put(tv, x, y, w, h, size)
    }

    // Tekst die je kunt aantikken
    protected fun button(
        label: String, x: Int, y: Int, w: Int, h: Int,
        size: Float = 30f, action: () -> Unit
    ): TextView {
        val v = text(label, x, y, w, h, size)
        v.background = RippleDrawable(ColorStateList.valueOf(0x55D6B654), box(), null)
        v.isFocusable = true
        v.setOnClickListener { action() }

        // Schermlezers moeten dit als knop herkennen
        v.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = "android.widget.Button"
            }
        }
        return v
    }

    // Invoerveld dat zijn waarde automatisch opslaat
    protected fun input(
        hintText: String, key: String, x: Int, y: Int, w: Int, h: Int,
        email: Boolean = false
    ): EditText {
        val field = EditText(this)
        field.id = View.generateViewId()
        field.hint = hintText
        field.setText(prefs.getString(key, ""))
        field.setTextColor(Color.WHITE)
        field.setHintTextColor(Color.rgb(130, 145, 151))
        field.gravity = Gravity.CENTER
        field.setPadding(0, 0, 0, 0)
        field.includeFontPadding = false
        field.setSingleLine(true)
        field.background = box()
        field.contentDescription = hintText

        // Toetsenbord: e-mail of gewone tekst
        if (email) {
            field.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        } else {
            field.inputType = InputType.TYPE_CLASS_TEXT
        }

        // Autofill werkt vanaf Android 8
        if (Build.VERSION.SDK_INT >= 26) {
            if (email) {
                field.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
            } else {
                field.setAutofillHints(View.AUTOFILL_HINT_POSTAL_ADDRESS)
            }
        }

        profileFields[key] = field

        // Bij elke wijziging opslaan en canvas opnieuw laten meten
        field.doAfterTextChanged { value ->
            val newText = value?.toString().orEmpty()
            prefs.edit().putString(key, newText).apply()
            field.requestLayout()
        }

        return put(field, x, y, w, h, 18f)
    }

    // ---------------------------------------------------------------
    // Onderdelen van een pagina
    // ---------------------------------------------------------------

    // Bovenbalk: logo, login, winkelwagen en zoekbalk
    protected fun header() {
        fun icon(res: Int, label: String, x: Int, w: Int, target: Class<out AppCompatActivity>) {
            val image = ImageView(this)
            image.setImageResource(res)
            image.scaleType = ImageView.ScaleType.FIT_CENTER
            image.contentDescription = label
            image.isFocusable = true
            image.setOnClickListener { navigate(target) }
            put(image, x, 76, w, 64)
        }

        icon(R.drawable.logo, "Home", 20, 125, home::class.java)
        icon(R.drawable.login, "Login", 176, 117, login::class.java)
        icon(R.drawable.winkelwagen, "Shopping cart", 312, 64, shopingcart::class.java)

        if (page == "game") {
            // Op de gamepagina zie je de gekozen game in plaats van de zoekbalk
            gameRow(selectedGame, 12, 166, 369, 48, clickable = false)
        } else {
            val search = EditText(this)
            search.hint = "Zoek balk"
            search.setHintTextColor(Color.rgb(238, 241, 242))
            search.setTextColor(Color.WHITE)
            search.gravity = Gravity.CENTER
            search.setPadding(0, 0, 0, 0)
            search.includeFontPadding = false
            search.setSingleLine(true)
            search.background = box()
            search.contentDescription = "Zoek balk"
            search.imeOptions = EditorInfo.IME_ACTION_SEARCH
            search.setOnEditorActionListener { _, _, _ ->
                if (search.text.isNotBlank()) {
                    navigate(game1::class.java)
                }
                false
            }
            put(search, 12, 166, 369, 48, 32f)
        }
    }

    // Brede titelbalk
    protected fun title(label: String) {
        val size = if (label == "Thanks for ordering") 38f else 44f
        val bar = text(label, 0, 248, 393, 60, size)
        bar.background = box(Color.rgb(9, 57, 65))
    }

    // Vier tabknoppen naast elkaar
    protected fun tabs(category: Boolean = false) {
        for (i in 0..3) {
            val label = if (category) "Categorie ${i + 1}" else "Thema ${i + 1}"
            val width = if (i == 3) 99 else 98
            val size = if (category) 16f else 22f

            val tab = button(label, i * 98, 248, width, 60, size) {
                if (category) {
                    Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
                } else {
                    navigate(thema::class.java)
                }
            }

            // Op de themapagina is de eerste tab gemarkeerd
            val selected = page == "thema" && i == 0
            val fill = if (selected) Color.rgb(100, 101, 52) else Color.rgb(9, 57, 65)
            tab.background = RippleDrawable(ColorStateList.valueOf(0x66D9B240), box(fill), null)
        }
    }

    // Eén rij met afbeelding en naam van een game
    protected fun gameRow(
        index: Int, x: Int, y: Int, w: Int, h: Int,
        compact: Boolean = false, clickable: Boolean = true
    ) {
        val i = index.coerceIn(0, gameNames.lastIndex)
        val imageWidth = if (compact) 52 else 86

        // De rij zelf: aantikbaar of alleen een vakje
        val row: TextView
        if (clickable) {
            row = button("", x, y, w, h) {
                prefs.edit().putInt("selectedGame", i).apply()
                navigate(game1::class.java)
            }
        } else {
            row = text("", x, y, w, h, border = true)
        }
        row.contentDescription = gameNames[i]

        // Afbeelding links
        val image = ImageView(this)
        image.setImageResource(gameImages[i])
        image.scaleType = ImageView.ScaleType.FIT_CENTER
        image.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        put(image, x + 5, y + 3, imageWidth, h - 6)

        // Naam rechts, op één regel met ... als hij te lang is
        val textSize = if (compact) 11f else 18f
        val name = text(gameNames[i], x + imageWidth + 10, y, w - imageWidth - 15, h, textSize)
        name.maxLines = 1
        name.ellipsize = TextUtils.TruncateAt.END
        name.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
}

/**
 * Eigen layout. Alle kinderen krijgen coordinaten in ontwerppixels (393 breed)
 * en deze klasse rekent ze om naar echte schermpixels.
 */
class DesignCanvas(context: Context) : ViewGroup(context) {

    // Positie, grootte en lettergrootte van een kind (in ontwerppixels)
    class Position(val x: Int, val y: Int, val w: Int, val h: Int, val font: Float) : LayoutParams(w, h)

    // Hoeveel echte pixels één ontwerppixel is
    private var factor = 1f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        factor = width / 393f

        // Hoogte: minstens 800 ontwerppixels, of de hoogte die de ScrollView geeft
        val minimumHeight = (800 * factor).roundToInt()
        var givenHeight = 0
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            givenHeight = MeasureSpec.getSize(heightMeasureSpec)
        }
        setMeasuredDimension(width, maxOf(minimumHeight, givenHeight))

        for (i in 0 until childCount) {
            val view = getChildAt(i)
            val p = view.layoutParams as Position

            if (view is TextView && p.font > 0) {
                // Lettergrootte schalen, inclusief de lettergrootte-instelling van de gebruiker
                val fontScale = resources.configuration.fontScale
                val normalSize = p.font * factor * fontScale
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, normalSize)

                // Lange invoer kleiner maken zodat hij in het veld past
                if (view is EditText && view.text.isNotEmpty()) {
                    val availableWidth = p.w * factor - view.paddingLeft - view.paddingRight - 8 * factor
                    val textWidth = view.paint.measureText(view.text.toString())
                    if (textWidth > availableWidth) {
                        val smallerSize = normalSize * availableWidth / textWidth
                        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallerSize)
                    }
                }
            }

            val widthSpec = MeasureSpec.makeMeasureSpec((p.w * factor).roundToInt(), MeasureSpec.EXACTLY)
            val heightSpec = MeasureSpec.makeMeasureSpec((p.h * factor).roundToInt(), MeasureSpec.EXACTLY)
            view.measure(widthSpec, heightSpec)
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