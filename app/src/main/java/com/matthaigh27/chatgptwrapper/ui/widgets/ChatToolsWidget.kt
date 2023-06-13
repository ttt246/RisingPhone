package com.matthaigh27.chatgptwrapper.ui.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import com.matthaigh27.chatgptwrapper.R


class ChatToolsWidget(context: Context) : LinearLayout(context), OnClickListener {

    private lateinit var mClickListener: OnPositiveButtonClickListener
    private lateinit var mGlTools: GridLayout
    private var isInitFlag = true

    val TOOL_ICONS = arrayOf(
        R.drawable.ic_camera,
        R.drawable.ic_gallery,
    )

    val TOOL_NAMES = arrayOf(
        "Camera",
        "Gallery"
    )

    val TOOL_COUNT = 2

    init {
        initView()
    }

    fun setOnClickListener(listener: OnPositiveButtonClickListener) {
        mClickListener = listener
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_chat_tools, this, true)

        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        mGlTools = findViewById(R.id.gl_tools)

        this.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (!isInitFlag) return@addOnLayoutChangeListener
            isInitFlag = false

            val width: Int = this.width

            for (i in 0 until TOOL_COUNT) {
                val cameraTool = ChatToolItem(context)
                val itemMargin =
                    context.resources.getDimensionPixelSize(R.dimen.item_chat_tool_margin)
                cameraTool.setTool(TOOL_ICONS[i], width / 4 - itemMargin * 2, TOOL_NAMES[i])
                mGlTools.addView(cameraTool)

                cameraTool.setTag(TOOL_NAMES[i])
                cameraTool.setOnClickListener(this@ChatToolsWidget)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.tag) {
            "Camera" -> {
                mClickListener.onPositiveBtnClick(true)
            }

            "Gallery" -> {
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