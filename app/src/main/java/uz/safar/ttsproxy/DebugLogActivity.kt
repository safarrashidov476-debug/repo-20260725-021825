package uz.safar.ttsproxy

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * A minimal, computer-free way to see what text the TTS engine is actually
 * receiving. Reads this app's own logcat output (no special permission needed -
 * an app can always read log lines it wrote itself) and shows it as plain,
 * selectable, screen-reader-friendly text.
 */
class DebugLogActivity : Activity() {

    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logView = TextView(this).apply {
            setTextIsSelectable(true)
            setPadding(32, 32, 32, 32)
            text = "Hali log yo'q. \"Yangilash\" tugmasini bosing yoki bir harfni o'qiting-da qayting."
        }

        val refreshButton = Button(this).apply {
            text = "Yangilash"
            setOnClickListener { refreshLog() }
        }

        val clearButton = Button(this).apply {
            text = "Tozalash"
            setOnClickListener {
                runCatching { Runtime.getRuntime().exec(arrayOf("logcat", "-c")) }
                logView.text = "Loglar tozalandi."
            }
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(refreshButton)
            addView(clearButton)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(buttonRow)
            addView(ScrollView(context).apply { addView(logView) })
        }

        setContentView(root)
        refreshLog()
    }

    override fun onResume() {
        super.onResume()
        refreshLog()
    }

    private fun refreshLog() {
        logView.text = runCatching {
            val process = Runtime.getRuntime().exec(
                arrayOf("logcat", "-d", "-v", "time", "-s", "PhoneGroupingTts:D")
            )
            val output = process.inputStream.bufferedReader().readText()
            output.ifBlank { "Hali hech qanday log yo'q. Klaviaturada bir harfni o'qiting-da, bu yerga qayting va \"Yangilash\"ni bosing." }
        }.getOrElse { e ->
            "Logni o'qib bo'lmadi: ${e.message}"
        }
    }
}
