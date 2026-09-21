package com.example.keysystem

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

class WaveView(ctx: Context) : View(ctx) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    init { paint.color = Color.parseColor("#1A5DA6") }
    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val sx = w / 665f
        val sy = h / 90f
        val p = Path()
        p.moveTo(0f, 0f)
        p.lineTo(w, 0f)
        p.lineTo(w, 79f * sy)
        p.cubicTo(520f * sx, 86f * sy, 380f * sx, 60f * sy, 256f * sx, 39f * sy)
        p.cubicTo(170f * sx, 42f * sy, 60f * sx, 68f * sy, 0f, 76f * sy)
        p.close()
        c.drawPath(p, paint)
    }
}

class LicenseActivity : Activity() {

    // TEST KEY: works offline. Remove before releasing.
    private val testKey = "ASH-TEST-2026-0001"
    // Your license server (returns HTTP 200 for a valid key)
    private val verifyUrl = "https://your-server.example.com/api/verify"

    private lateinit var etKey: EditText
    private lateinit var tvStatus: TextView

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun col(hex: String): Int = Color.parseColor(hex)

    private fun rounded(fill: String, radius: Int, stroke: String? = null): GradientDrawable {
        val d = GradientDrawable()
        d.setColor(col(fill))
        d.cornerRadius = dp(radius).toFloat()
        if (stroke != null) d.setStroke(dp(1), col(stroke))
        return d
    }

    private fun label(t: String, sp: Float, color: String, bold: Boolean = false, spacing: Float = 0f): TextView {
        val v = TextView(this)
        v.text = t
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        v.setTextColor(col(color))
        v.letterSpacing = spacing
        if (bold) v.typeface = Typeface.DEFAULT_BOLD
        return v
    }

    private fun lp(w: Int, h: Int, l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(w, h)
        p.setMargins(dp(l), dp(t), dp(r), dp(b))
        return p
    }

    private fun divider(): View {
        val v = View(this)
        v.setBackgroundColor(col("#E6EEF8"))
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
        val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.background = rounded("#FFFFFF", 28)
        card.clipToOutline = true

        // Header
        val header = FrameLayout(this)
        header.addView(WaveView(this), FrameLayout.LayoutParams(MATCH, MATCH))
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val icon = label("≡", 18f, "#FFFFFF")
        icon.gravity = Gravity.CENTER
        icon.background = rounded("#3C78B8", 12)
        row.addView(icon, LinearLayout.LayoutParams(dp(30), dp(30)))
        val titles = LinearLayout(this)
        titles.orientation = LinearLayout.VERTICAL
        val title = label("GUI MODS", 20f, "#FFFFFF", true, 0.15f)
        title.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        titles.addView(title)
        titles.addView(label("Ash System", 9f, "#8FC0F5", false, 0.25f), lp(WRAP, WRAP, t = 6))
        row.addView(titles, lp(WRAP, WRAP, l = 14))
        val rowLp = FrameLayout.LayoutParams(WRAP, WRAP, Gravity.CENTER_HORIZONTAL)
        rowLp.topMargin = dp(16)
        header.addView(row, rowLp)
        card.addView(header, lp(MATCH, dp(90)))

        // Version row
        val ver = LinearLayout(this)
        ver.orientation = LinearLayout.HORIZONTAL
        ver.gravity = Gravity.CENTER
        ver.addView(label("Ash System", 10f, "#2B7FE0", false, 0.25f))
        val badge = label("V21", 10f, "#1A5DA6")
        badge.background = rounded("#EAF2FF", 12, "#B5D3F5")
        badge.setPadding(dp(10), dp(2), dp(10), dp(2))
        ver.addView(badge, lp(WRAP, WRAP, l = 8))
        card.addView(ver, lp(MATCH, WRAP, t = 10, b = 8))
        card.addView(divider(), lp(MATCH, dp(1), l = 16, r = 16))

        // License key box
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.background = rounded("#EAF2FF", 20, "#B5D3F5")
        box.setPadding(dp(14), dp(14), dp(14), dp(14))
        box.addView(label("LICENSE KEY", 9f, "#1A5DA6", true, 0.2f))
        etKey = EditText(this)
        etKey.setBackgroundColor(Color.TRANSPARENT)
        etKey.gravity = Gravity.TOP
        etKey.setTextColor(col("#0F3D73"))
        etKey.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        etKey.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        etKey.setPadding(0, dp(6), 0, 0)
        box.addView(etKey, lp(MATCH, dp(60)))
        card.addView(box, lp(MATCH, WRAP, l = 16, t = 12, r = 16))

        // Paste button
        val paste = label("TAP TO PASTE", 13f, "#1A5DA6", true)
        paste.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        paste.background = rounded("#E8F1FC", 28, "#B5D3F5")
        paste.setPadding(dp(26), dp(14), dp(26), dp(14))
        val pasteLp = lp(WRAP, WRAP, t = 8)
        pasteLp.gravity = Gravity.CENTER_HORIZONTAL
        card.addView(paste, pasteLp)

        // Verify button
        val verify = label("VERIFY LICENSE", 14f, "#FFFFFF", true, 0.1f)
        verify.gravity = Gravity.CENTER
        val grad = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(col("#2B8CFF"), col("#1A5DA6"))
        )
        grad.cornerRadius = dp(32).toFloat()
        verify.background = grad
        verify.elevation = dp(6).toFloat()
        card.addView(verify, lp(MATCH, dp(52), l = 16, t = 10, r = 16))

        // Status + footer
        tvStatus = label("Enter your license key to continue", 11f, "#8FAED6")
        tvStatus.gravity = Gravity.CENTER
        card.addView(tvStatus, lp(MATCH, WRAP, t = 8))
        card.addView(divider(), lp(MATCH, dp(1), l = 16, t = 10, r = 16))
        val foot = label("Unauthorized access is prohibited", 9f, "#8FAED6", false, 0.12f)
        foot.gravity = Gravity.CENTER
        foot.setPadding(0, dp(10), 0, dp(16))
        card.addView(foot, lp(MATCH, WRAP))

        // Screen
        val root = FrameLayout(this)
        root.setPadding(dp(12), dp(16), dp(12), dp(16))
        val cardW = minOf(dp(360), resources.displayMetrics.widthPixels - dp(24))
        root.addView(card, FrameLayout.LayoutParams(cardW, WRAP, Gravity.CENTER))
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.BLACK)
        scroll.isFillViewport = true
        scroll.add
