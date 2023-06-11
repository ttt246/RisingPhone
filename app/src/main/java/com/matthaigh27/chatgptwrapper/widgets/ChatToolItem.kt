package com.matthaigh27.chatgptwrapper.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.R.styleable.ChatToolLayout

class ChatToolItem(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private lateinit var mContext:Context
    private lateinit var mIvToolIcon: ImageView
    private lateinit var mTvToolName: TextView
    private lateinit var mClToolIcon: ConstraintLayout

    init {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.item_chat_tool, this, true)

        val layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val itemMargin = context.resources.getDimensionPixelSize(R.dimen.item_chat_tool_margin)
        layoutParams.setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
        this.layoutParams = layoutParams

        mContext = context
        mIvToolIcon = findViewById(R.id.iv_chat_tool)
        mClToolIcon = findViewById(R.id.cl_chat_tool)
        mTvToolName = findViewById(R.id.tv_chat_tool_description)

    }

    fun setTool(drawableId: Int, size: Int, name: String) {
        mIvToolIcon.setImageResource(drawableId)
        mClToolIcon.layoutParams = LayoutParams(size, size)
        mTvToolName.text = name
    }
}