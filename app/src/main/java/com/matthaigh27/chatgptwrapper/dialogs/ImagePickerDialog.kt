package com.matthaigh27.chatgptwrapper.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import com.matthaigh27.chatgptwrapper.R


class ImagePickerDialog(context: Context) : Dialog(context), OnClickListener {

    private lateinit var mClickListener: OnPositiveButtonClickListener

    init {
        setCancelable(false)
    }

    fun setOnClickListener(listener: OnPositiveButtonClickListener) {
        mClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_file_picker)

        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        findViewById<LinearLayout>(R.id.lytCameraPick).setOnClickListener(this)
        findViewById<LinearLayout>(R.id.lytGalleryPick).setOnClickListener(this)

        setCanceledOnTouchOutside(true)
    }

    override fun onClick(view: View?) {
        this.hide()

        when (view?.id) {
            R.id.lytCameraPick -> {
                mClickListener.onPositiveBtnClick(true)
            }
            R.id.lytGalleryPick -> {
                mClickListener.onPositiveBtnClick(false)
            }
        }
    }

    /**
     * callback function invoked when filepickerdialog buttons are pressed
     */
    interface OnPositiveButtonClickListener {
        fun onPositiveBtnClick(isCamera: Boolean?)
    }
}