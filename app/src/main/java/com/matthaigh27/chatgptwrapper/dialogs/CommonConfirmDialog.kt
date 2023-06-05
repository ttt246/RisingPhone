package com.matthaigh27.chatgptwrapper.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.matthaigh27.chatgptwrapper.R

class CommonConfirmDialog(context: Context) : Dialog(context), View.OnClickListener {

    private var mTvMessage: TextView? = null
    private var mMessage: String = ""
    private lateinit var mClickListener: OnConfirmButtonClickListener

    init {
        setCancelable(false)
    }

    fun setOnClickListener(listener: OnConfirmButtonClickListener) {
        mClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
    }

    private fun initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_common_confirm)

        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        findViewById<Button>(R.id.btn_ok).setOnClickListener(this)
        findViewById<Button>(R.id.btn_cancel).setOnClickListener(this)

        mTvMessage = findViewById(R.id.tv_message)

        setCanceledOnTouchOutside(true)
    }

    fun setMessage(message: String) {
        mMessage = message
        mTvMessage?.text = mMessage
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_ok -> {
                mClickListener.onPositiveButtonClick()
            }
            R.id.btn_cancel -> {
                mClickListener.OnNegativeButtonClick()
            }
        }

        this.dismiss()
    }

    interface OnConfirmButtonClickListener {
        fun onPositiveButtonClick()
        fun OnNegativeButtonClick()
    }
}