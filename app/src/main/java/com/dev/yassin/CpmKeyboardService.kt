package com.dev.yassin

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow

class CpmKeyboardService : InputMethodService() {

    private var isShifted = false
    private var currentPopup: PopupWindow? = null

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        val layoutArabic = view.findViewById<LinearLayout>(R.id.layout_arabic)
        val layoutEnglish = view.findViewById<LinearLayout>(R.id.layout_english)
        val layoutSymbolsAr = view.findViewById<LinearLayout>(R.id.layout_symbols_ar)
        val layoutSymbolsEn = view.findViewById<LinearLayout>(R.id.layout_symbols_en)
        val layoutNumpadAr = view.findViewById<LinearLayout>(R.id.layout_numpad_ar)
        val layoutNumpadEn = view.findViewById<LinearLayout>(R.id.layout_numpad_en)
        val layoutSymbolsExtraAr = view.findViewById<LinearLayout>(R.id.layout_symbols_extra_ar)
        val layoutSymbolsExtraEn = view.findViewById<LinearLayout>(R.id.layout_symbols_extra_en)

        fun bindKeys(map: Map<Int, String>) {
            for ((id, char) in map) {
                view.findViewById<Button>(id)?.setOnClickListener {
                    currentInputConnection.commitText(char, 1)
                }
            }
        }

        // ربط الحروف
        bindKeys(mapOf(
            R.id.ar_ض to "ض", R.id.ar_ص to "ص", R.id.ar_ث to "ث", R.id.ar_ق to "ق",
            R.id.ar_ف to "ف", R.id.ar_غ to "غ", R.id.ar_ع to "ع", R.id.ar_ه to "ه",
            R.id.ar_خ to "خ", R.id.ar_ح to "ح", R.id.ar_ج to "ج", R.id.ar_ش to "ش",
            R.id.ar_س to "س", R.id.ar_ي to "ي", R.id.ar_ب to "ب", R.id.ar_ل to "ل",
            R.id.ar_ا to "ا", R.id.ar_ت to "ت", R.id.ar_ن to "ن", R.id.ar_م to "م",
            R.id.ar_ك to "ك", R.id.ar_ط to "ط", R.id.ar_ذ to "ذ", R.id.ar_ء to "ء",
            R.id.ar_ؤ to "ؤ", R.id.ar_ر to "ر", R.id.ar_ى to "ى", R.id.ar_ة to "ة",
            R.id.ar_و to "و", R.id.ar_ز to "ز", R.id.ar_ظ to "ظ", R.id.ar_د to "د"
        ))

        // تفعيل الضغط المطول للفاء
        setupLongPress(view.findViewById(R.id.ar_ف), listOf("ڤ", "ڥ", "ڦ"))

        // تفعيل الضغط المطول للنقطة (علامات التشكيل) عربي
        setupLongPress(view.findViewById(R.id.sym_ar_dot), listOf("َ", "ً", "ٍ", "ٌ", "ُ", "ِ", "ْ", "ّ", "ٰ", "ٓ", "ٕ", "ٖ"))
        
        // تفعيل الضغط المطول للنقطة (رموز) إنجليزي
        setupLongPress(view.findViewById(R.id.sym_en_dot), listOf("&", "%", "+", "\"", "-", ":", "'", "@", ";", "/", "(", ")", "#", "!"))

        // ربط الحروف الإنجليزية
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
                if (isShifted) { isShifted = false; updateShiftState(view) }
            }
        }

        // ربط أرقام ورموز العربية
        bindKeys(mapOf(
            R.id.sym_ar_1 to "١", R.id.sym_ar_2 to "٢", R.id.sym_ar_3 to "٣", R.id.sym_ar_4 to "٤", R.id.sym_ar_5 to "٥",
            R.id.sym_ar_6 to "٦", R.id.sym_ar_7 to "٧", R.id.sym_ar_8 to "٨", R.id.sym_ar_9 to "٩", R.id.sym_ar_0 to "٠",
            R.id.sym_ar_at to "@", R.id.sym_ar_hash to "#", R.id.sym_ar_dollar to "$", R.id.sym_ar_underscore to "_", R.id.sym_ar_amp to "&",
            R.id.sym_ar_minus to "-", R.id.sym_ar_plus to "+", R.id.sym_ar_paren1 to "(", R.id.sym_ar_paren2 to ")", R.id.sym_ar_slash to "/",
            R.id.sym_ar_star to "*", R.id.sym_ar_quote to "\"", R.id.sym_ar_apostrophe to "'", R.id.sym_ar_colon to ":", R.id.sym_ar_semicolon to ";",
            R.id.sym_ar_excl to "!", R.id.sym_ar_quest to "?", R.id.sym_ar_comma to "،", R.id.sym_ar_dot to "."
        ))

        // ربط رموز Numpad العربي
        bindKeys(mapOf(
            R.id.num_ar_plus to "+", R.id.num_ar_1 to "1", R.id.num_ar_2 to "2", R.id.num_ar_3 to "3", R.id.num_ar_perc to "%",
            R.id.num_ar_minus to "-", R.id.num_ar_4 to "4", R.id.num_ar_5 to "5", R.id.num_ar_6 to "6", R.id.num_ar_lbrack to "[",
            R.id.num_ar_mul to "*", R.id.num_ar_7 to "7", R.id.num_ar_8 to "8", R.id.num_ar_9 to "9",
            R.id.num_ar_comma to "،", R.id.num_ar_0 to "0", R.id.num_ar_eq to "=", R.id.num_ar_dot to "."
        ))

        // ربط أرقام ورموز الإنجليزية
        bindKeys(mapOf(
            R.id.sym_en_1 to "1", R.id.sym_en_2 to "2", R.id.sym_en_3 to "3", R.id.sym_en_4 to "4", R.id.sym_en_5 to "5",
            R.id.sym_en_6 to "6", R.id.sym_en_7 to "7", R.id.sym_en_8 to "8", R.id.sym_en_9 to "9", R.id.sym_en_0 to "0",
            R.id.sym_en_at to "@", R.id.sym_en_hash to "#", R.id.sym_en_dollar to "$", R.id.sym_en_underscore to "_", R.id.sym_en_amp to "&",
            R.id.sym_en_minus to "-", R.id.sym_en_plus to "+", R.id.sym_en_paren1 to "(", R.id.sym_en_paren2 to ")", R.id.sym_en_slash to "/",
            R.id.sym_en_star to "*", R.id.sym_en_quote to "\"", R.id.sym_en_apostrophe to "'", R.id.sym_en_colon to ":", R.id.sym_en_semicolon to ";",
            R.id.sym_en_excl to "!", R.id.sym_en_quest to "?", R.id.sym_en_comma to ",", R.id.sym_en_dot to "."
        ))

        // ربط رموز Numpad الإنجليزي
        bindKeys(mapOf(
            R.id.num_en_plus to "+", R.id.num_en_1 to "1", R.id.num_en_2 to "2", R.id.num_en_3 to "3", R.id.num_en_perc to "%",
            R.id.num_en_minus to "-", R.id.num_en_4 to "4", R.id.num_en_5 to "5", R.id.num_en_6 to "6", R.id.num_en_lbrack to "[",
            R.id.num_en_mul to "*", R.id.num_en_7 to "7", R.id.num_en_8 to "8", R.id.num_en_9 to "9",
            R.id.num_en_comma to ",", R.id.num_en_0 to "0", R.id.num_en_eq to "=", R.id.num_en_dot to "."
        ))

        // ربط الرموز الإضافية (عربي)
        bindKeys(mapOf(
            R.id.sym_ex_ar_tilde to "~", R.id.sym_ex_ar_backtick to "`", R.id.sym_ex_ar_pipe to "|", R.id.sym_ex_ar_bullet to "•", R.id.sym_ex_ar_sqrt to "√",
            R.id.sym_ex_ar_pi to "π", R.id.sym_ex_ar_div to "÷", R.id.sym_ex_ar_mul to "×", R.id.sym_ex_ar_sect to "§", R.id.sym_ex_ar_delta to "Δ",
            R.id.sym_ex_ar_pound to "£", R.id.sym_ex_ar_cent to "¢", R.id.sym_ex_ar_euro to "€", R.id.sym_ex_ar_yen to "¥", R.id.sym_ex_ar_pow to "^",
            R.id.sym_ex_ar_deg to "°", R.id.sym_ex_ar_eq2 to "=", R.id.sym_ex_ar_lbrace to "{", R.id.sym_ex_ar_rbrace to "}", R.id.sym_ex_ar_backslash to "\\",
            R.id.sym_ex_ar_perc to "%", R.id.sym_ex_ar_copy to "©", R.id.sym_ex_ar_reg to "®", R.id.sym_ex_ar_tm to "™", R.id.sym_ex_ar_check to "✓",
            R.id.sym_ex_ar_lbrack to "[", R.id.sym_ex_ar_rbrack to "]", R.id.sym_ex_ar_gt to ">"
        ))

        // ربط الرموز الإضافية (إنجليزي)
        bindKeys(mapOf(
            R.id.sym_ex_en_tilde to "~", R.id.sym_ex_en_backtick to "`", R.id.sym_ex_en_pipe to "|", R.id.sym_ex_en_bullet to "•", R.id.sym_ex_en_sqrt to "√",
            R.id.sym_ex_en_pi to "π", R.id.sym_ex_en_div to "÷", R.id.sym_ex_en_mul to "×", R.id.sym_ex_en_sect to "§", R.id.sym_ex_en_delta to "Δ",
            R.id.sym_ex_en_pound to "£", R.id.sym_ex_en_cent to "¢", R.id.sym_ex_en_euro to "€", R.id.sym_ex_en_yen to "¥", R.id.sym_ex_en_pow to "^",
            R.id.sym_ex_en_deg to "°", R.id.sym_ex_en_eq2 to "=", R.id.sym_ex_en_lbrace to "{", R.id.sym_ex_en_rbrace to "}", R.id.sym_ex_en_backslash to "\\",
            R.id.sym_ex_en_perc to "%", R.id.sym_ex_en_copy to "©", R.id.sym_ex_en_reg to "®", R.id.sym_ex_en_tm to "™", R.id.sym_ex_en_check to "✓",
            R.id.sym_ex_en_lbrack to "[", R.id.sym_ex_en_rbrack to "]", R.id.sym_ex_en_gt to ">"
        ))

        // المسافة والإنتر
        val spaces = listOf(R.id.btn_space_ar, R.id.btn_space_en, R.id.btn_space_ar_sym, R.id.btn_space_en_sym, R.id.btn_space_ar_extra, R.id.btn_space_en_extra)
        for (id in spaces) view.findViewById<Button>(id)?.setOnClickListener { currentInputConnection.commitText(" ", 1) }

        val enters = listOf(R.id.btn_enter_ar, R.id.btn_enter_en, R.id.btn_enter_ar_sym, R.id.btn_enter_en_sym, R.id.btn_enter_ar_extra, R.id.btn_enter_en_extra, R.id.btn_enter_ar_num, R.id.btn_enter_en_num)
        for (id in enters) view.findViewById<ImageButton>(id)?.setOnClickListener { currentInputConnection.commitText("\n", 1) }

        // الحذف
        val dels = listOf(R.id.btn_del_ar, R.id.btn_del_en, R.id.btn_del_ar_sym, R.id.btn_del_en_sym, R.id.btn_del_ar_extra, R.id.btn_del_en_extra, R.id.btn_del_ar_num, R.id.btn_del_en_num)
        for (id in dels) setupContinuousDelete(view.findViewById(id))

        // التنقل بين الصفحات (عربي)
        view.findViewById<Button>(R.id.btn_123_ar)?.setOnClickListener { layoutArabic.visibility = View.GONE; layoutSymbolsAr.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_abc_ar)?.setOnClickListener { layoutSymbolsAr.visibility = View.GONE; layoutArabic.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_more_sym_ar)?.setOnClickListener { layoutSymbolsAr.visibility = View.GONE; layoutSymbolsExtraAr.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_back_to_sym_ar)?.setOnClickListener { layoutSymbolsExtraAr.visibility = View.GONE; layoutSymbolsAr.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_abc_ar_extra)?.setOnClickListener { layoutSymbolsExtraAr.visibility = View.GONE; layoutArabic.visibility = View.VISIBLE }
        // Numpad عربي
        view.findViewById<Button>(R.id.btn_numpad_ar)?.setOnClickListener { layoutSymbolsAr.visibility = View.GONE; layoutNumpadAr.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_switch_from_num_ar)?.setOnClickListener { layoutNumpadAr.visibility = View.GONE; layoutSymbolsAr.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_numpad_ar_extra)?.setOnClickListener { layoutSymbolsExtraAr.visibility = View.GONE; layoutNumpadAr.visibility = View.VISIBLE }

        // التنقل بين الصفحات (إنجليزي)
        view.findViewById<Button>(R.id.btn_123_en)?.setOnClickListener { layoutEnglish.visibility = View.GONE; layoutSymbolsEn.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_abc_en)?.setOnClickListener { layoutSymbolsEn.visibility = View.GONE; layoutEnglish.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_more_sym_en)?.setOnClickListener { layoutSymbolsEn.visibility = View.GONE; layoutSymbolsExtraEn.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_back_to_sym_en)?.setOnClickListener { layoutSymbolsExtraEn.visibility = View.GONE; layoutSymbolsEn.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_abc_en_extra)?.setOnClickListener { layoutSymbolsExtraEn.visibility = View.GONE; layoutEnglish.visibility = View.VISIBLE }
        // Numpad إنجليزي
        view.findViewById<Button>(R.id.btn_numpad_en)?.setOnClickListener { layoutSymbolsEn.visibility = View.GONE; layoutNumpadEn.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_switch_from_num_en)?.setOnClickListener { layoutNumpadEn.visibility = View.GONE; layoutSymbolsEn.visibility = View.VISIBLE }
        view.findViewById<Button>(R.id.btn_numpad_en_extra)?.setOnClickListener { layoutSymbolsExtraEn.visibility = View.GONE; layoutNumpadEn.visibility = View.VISIBLE }

        // تحويل اللغة
        view.findViewById<ImageButton>(R.id.btn_lang_to_en)?.setOnClickListener { layoutArabic.visibility = View.GONE; layoutEnglish.visibility = View.VISIBLE }
        view.findViewById<ImageButton>(R.id.btn_lang_to_ar)?.setOnClickListener { layoutEnglish.visibility = View.GONE; layoutArabic.visibility = View.VISIBLE }

        // الشيفت
        view.findViewById<ImageButton>(R.id.en_shift)?.setOnClickListener {
            isShifted = !isShifted
            updateShiftState(view)
        }

        return view
    }

    // --- دوال Popup والحذف ---
    @SuppressLint("ClickableViewAccessibility")
    private fun setupLongPress(btn: View?, alternativeChars: List<String>) {
        if (btn == null || alternativeChars.isEmpty()) return
        val handler = Handler(Looper.getMainLooper())
        var isLongPressed = false
        var longPressRunnable: Runnable? = null

        btn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isLongPressed = false
                    longPressRunnable = Runnable { isLongPressed = true; showPopup(v, alternativeChars) }
                    handler.postDelayed(longPressRunnable!!, 400)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (longPressRunnable != null) handler.removeCallbacks(longPressRunnable!!)
                    if (!isLongPressed) {
                        val text = when(v) {
                            is Button -> v.text.toString()
                            else -> ""
                        }
                        if (text.isNotEmpty()) currentInputConnection.commitText(text, 1)
                    } else {
                        currentPopup?.dismiss()
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (longPressRunnable != null) handler.removeCallbacks(longPressRunnable!!)
                    currentPopup?.dismiss()
                    true
                }
                else -> false
            }
        }
    }

    private fun showPopup(anchorView: View, chars: List<String>) {
        currentPopup?.dismiss()
        val context = anchorView.context
        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#444444"))
            setPadding(10, 10, 10, 10)
        }
        for (char in chars) {
            val charBtn = Button(context).apply {
                text = char
                textSize = 22f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.TRANSPARENT)
                minWidth = 0
                minimumWidth = 0
                setPadding(15, 5, 15, 5)
                setOnClickListener {
                    currentInputConnection.commitText(char, 1)
                    currentPopup?.dismiss()
                }
            }
            linearLayout.addView(charBtn)
        }
        val popup = PopupWindow(linearLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.elevation = 10f
        popup.showAsDropDown(anchorView, 0, -anchorView.height - 140) // تظبيط مكان الظهور فوق
        currentPopup = popup
    }

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
                            if (isDeleting) { currentInputConnection?.deleteSurroundingText(1, 0); handler.postDelayed(this, 20) }
                        }
                    }
                    handler.postDelayed(deleteRunnable!!, 250)
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
            view.findViewById<Button>(id)?.text = if (isShifted) char.uppercase() else char
        }
        val shiftBtn = view.findViewById<ImageButton>(R.id.en_shift)
        if (isShifted) shiftBtn?.setColorFilter(Color.parseColor("#4285F4")) else shiftBtn?.setColorFilter(Color.parseColor("#FFFFFF"))
    }
}
