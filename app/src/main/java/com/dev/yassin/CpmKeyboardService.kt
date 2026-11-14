package com.dev.yassin

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class CpmKeyboardService : InputMethodService() {

    private var isShifted = false

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        val layoutArabic = view.findViewById<LinearLayout>(R.id.layout_arabic)
        val layoutEnglish = view.findViewById<LinearLayout>(R.id.layout_english)

        // --- ربط الحروف العربية ---
        val arabicKeys = mapOf(
            R.id.ar_ض to "ض", R.id.ar_ص to "ص", R.id.ar_ث to "ث", R.id.ar_ق to "ق",
            R.id.ar_ف to "ف", R.id.ar_غ to "غ", R.id.ar_ع to "ع", R.id.ar_ه to "ه",
            R.id.ar_خ to "خ", R.id.ar_ح to "ح", R.id.ar_ج to "ج", R.id.ar_ش to "ش",
            R.id.ar_س to "س", R.id.ar_ي to "ي", R.id.ar_ب to "ب", R.id.ar_ل to "ل",
            R.id.ar_ا to "ا", R.id.ar_ت to "ت", R.id.ar_ن to "ن", R.id.ar_م to "م",
            R.id.ar_ك to "ك", R.id.ar_ط to "ط", R.id.ar_ذ to "ذ", R.id.ar_ء to "ء",
            R.id.ar_ؤ to "ؤ", R.id.ar_ر to "ر", R.id.ar_ى to "ى", R.id.ar_ة to "ة",
            R.id.ar_و to "و", R.id.ar_ز to "ز", R.id.ar_ظ to "ظ", R.id.ar_د to "د"
        )

        for ((id, char) in arabicKeys) {
            view.findViewById<Button>(id)?.setOnClickListener {
                currentInputConnection.commitText(char, 1)
            }
        }

        // --- ربط الحروف الإنجليزية (مع دعم الشيفت) ---
        val englishKeys = mapOf(
            R.id.en_q to "q", R.id.en_w to "w", R.id.en_e to "e", R.id.en_r to "r",
            R.id.en_t to "t", R.id.en_y to "y", R.id.en_u to "u", R.id.en_i to "i",
            R.id.en_o to "o", R.id.en_p to "p", R.id.en_a to "a", R.id.en_s to "s",
            R.id.en_d to "d", R.id.en_f to "f", R.id.en_g to "g", R.id.en_h to "h",
            R.id.en_j to "j", R.id.en_k to "k", R.id.en_l to "l", R.id.en_z to "z",
            R.id.en_x to "x", R.id.en_c to "c", R.id.en_v to "v", R.id.en_b to "b",
            R.id.en_n to "n", R.id.en_m to "m"
        )

        for ((id, char) in englishKeys) {
            view.findViewById<Button>(id)?.setOnClickListener {
                val textToCommit = if (isShifted) char.uppercase() else char
                currentInputConnection.commitText(textToCommit, 1)
                // لو الشيفت شغال، بعد ما تكتب حرف واحد يرجع small
                if (isShifted) {
                    isShifted = false
                    updateShiftState(view)
                }
            }
        }

        // --- زرار الشيفت ---
        view.findViewById<Button>(R.id.en_shift)?.setOnClickListener {
            isShifted = !isShifted
            updateShiftState(view)
        }

        // --- أزرار المسافة والإنتر (عربي) ---
        view.findViewById<Button>(R.id.btn_space_ar)?.setOnClickListener { currentInputConnection.commitText(" ", 1) }
        view.findViewById<Button>(R.id.btn_enter_ar)?.setOnClickListener { currentInputConnection.commitText("\n", 1) }

        // --- أزرار المسافة والإنتر (إنجليزي) ---
        view.findViewById<Button>(R.id.btn_space_en)?.setOnClickListener { currentInputConnection.commitText(" ", 1) }
        view.findViewById<Button>(R.id.btn_enter_en)?.setOnClickListener { currentInputConnection.commitText("\n", 1) }

        // --- إعداد زرار الحذف (الضغط المستمر) ---
        setupContinuousDelete(view.findViewById(R.id.btn_del_ar))
        setupContinuousDelete(view.findViewById(R.id.btn_del_en))

        // --- زرار تحويل اللغة ---
        view.findViewById<Button>(R.id.btn_lang_to_en)?.setOnClickListener {
            layoutArabic.visibility = View.GONE
            layoutEnglish.visibility = View.VISIBLE
        }

        view.findViewById<Button>(R.id.btn_lang_to_ar)?.setOnClickListener {
            layoutEnglish.visibility = View.GONE
            layoutArabic.visibility = View.VISIBLE
        }

        return view
    }

    // دالة عشان تمسح حرف حرف ولو فضلت ضاغط بتمسح ورا بعضها
    @SuppressLint("ClickableViewAccessibility")
    private fun setupContinuousDelete(btn: View?) {
        val handler = Handler(Looper.getMainLooper())
        var isDeleting = false
        var deleteRunnable: Runnable? = null

        btn?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDeleting = true
                    currentInputConnection?.deleteSurroundingText(1, 0)
                    deleteRunnable = object : Runnable {
                        override fun run() {
                            if (isDeleting) {
                                currentInputConnection?.deleteSurroundingText(1, 0)
                                handler.postDelayed(this, 50) // سرعة الحذف
                            }
                        }
                    }
                    handler.postDelayed(deleteRunnable!!, 400) // تأخير قبل ما يبدأ الحذف المستمر
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDeleting = false
                    deleteRunnable?.let { handler.removeCallbacks(it) }
                    true
                }
                else -> false
            }
        }
    }

    // دالة تحديث حالة الشيفت (الكابيتال أو سمول)
    private fun updateShiftState(view: View) {
        val englishKeys = listOf(
            R.id.en_q to "q", R.id.en_w to "w", R.id.en_e to "e", R.id.en_r to "r",
            R.id.en_t to "t", R.id.en_y to "y", R.id.en_u to "u", R.id.en_i to "i",
            R.id.en_o to "o", R.id.en_p to "p", R.id.en_a to "a", R.id.en_s to "s",
            R.id.en_d to "d", R.id.en_f to "f", R.id.en_g to "g", R.id.en_h to "h",
            R.id.en_j to "j", R.id.en_k to "k", R.id.en_l to "l", R.id.en_z to "z",
            R.id.en_x to "x", R.id.en_c to "c", R.id.en_v to "v", R.id.en_b to "b",
            R.id.en_n to "n", R.id.en_m to "m"
        )

        for ((id, char) in englishKeys) {
            val btn = view.findViewById<Button>(id)
            btn?.text = if (isShifted) char.uppercase() else char
        }

        // تغيير لون زرار الشيفت لما يكون شغال
        val shiftBtn = view.findViewById<Button>(R.id.en_shift)
        shiftBtn?.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (isShifted) android.graphics.Color.parseColor("#4285F4") else android.graphics.Color.TRANSPARENT
        )
    }
}
