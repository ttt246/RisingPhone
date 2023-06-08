package com.matthaigh27.chatgptwrapper.widgets

import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.matthaigh27.chatgptwrapper.R


class ImagePickerWidget(context: Context) : LinearLayout(context), OnClickListener {

    private lateinit var mClickListener: OnPositiveButtonClickListener

    init {
        initView()
    }

    fun setOnClickListener(listener: OnPositiveButtonClickListener) {
        mClickListener = listener
    }

    private fun initView() {
        inflate(context, R.layout.view_image_picker, this)

        layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        findViewById<LinearLayout>(R.id.lytCameraPick).setOnClickListener(this)
        findViewById<LinearLayout>(R.id.lytGalleryPick).setOnClickListener(this)
    }

    override fun onClick(view: View?) {
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