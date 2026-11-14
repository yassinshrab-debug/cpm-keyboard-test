package com.dev.yassin

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button

class CpmKeyboardService : InputMethodService() {

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        // ربط الأزرار بالكود
        val btnQ = view.findViewById<Button>(R.id.btn_q)
        val btnW = view.findViewById<Button>(R.id.btn_w)
        val btnSpace = view.findViewById<Button>(R.id.btn_space)
        val btnDel = view.findViewById<Button>(R.id.btn_del)
        val btnEnter = view.findViewById<Button>(R.id.btn_enter)

        // الأوامر لما تدوس على الزرار
        btnQ.setOnClickListener { currentInputConnection.commitText("q", 1) }
        btnW.setOnClickListener { currentInputConnection.commitText("w", 1) }
        btnSpace.setOnClickListener { currentInputConnection.commitText(" ", 1) }
        
        btnDel.setOnClickListener { 
            currentInputConnection.deleteSurroundingText(1, 0) 
        }
        
        btnEnter.setOnClickListener { 
            currentInputConnection.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
        }

        return view
    }
}
