package com.matthaigh27.chatgptwrapper.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.matthaigh27.chatgptwrapper.R


class SmsEditorWidget(
    context: Context, attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

    private val mContext: Context

    private val etToName: EditText
    private val etMessage: EditText

    private val btnConfirm: Button
    private val btnCancel: Button

    private var mListener: OnClickListener? = null

    init {
        inflate(context, R.layout.view_sms_editor, this)
        mContext = context

        layoutParams = LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        etToName = findViewById(R.id.et_to_name)
        etMessage = findViewById(R.id.et_message)

        btnConfirm = findViewById(R.id.btn_confirm)
        btnCancel = findViewById(R.id.btn_cancel)

        btnConfirm.setOnClickListener {
            if (etToName.text.toString().isEmpty() || etMessage.text.toString().isEmpty()) {
                Toast.makeText(
                    mContext, "Please input phone number and message.", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            mListener!!.confirmSMS(etToName.text.toString(), etMessage.text.toString())
            hide()
        }

        btnCancel.setOnClickListener {
            hide()
            mListener!!.cancelSMS()
        }
    }

    fun hide() {
        this.visibility = View.GONE
        etToName.setText("")
        etMessage.setText("")
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }

    interface OnClickListener {
        fun confirmSMS(phonenumber: String, message: String);
        fun cancelSMS();
    }
}