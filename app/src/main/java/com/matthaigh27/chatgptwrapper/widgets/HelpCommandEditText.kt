package com.matthaigh27.chatgptwrapper.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.Dimension
import com.matthaigh27.chatgptwrapper.R

@SuppressLint("AppCompatCustomView")
class HelpCommandEditText(context: Context) : EditText(context) {
    init {
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val marginBottom =
            context.resources.getDimensionPixelSize(R.dimen.view_help_prompt_key_edittext_margin_bottom)
        layoutParams.setMargins(0, 0, 0, marginBottom)
        this.layoutParams = layoutParams

        minHeight =
            context.resources.getDimensionPixelSize(R.dimen.view_help_prompt_key_edittext_minheight)

        val paddingStart =
            context.resources.getDimensionPixelSize(R.dimen.view_help_prompt_edittext_padding_start)
        setPadding(paddingStart, 0, 0, 0)


        setTextSize(
            Dimension.DP,
            context.resources.getDimensionPixelSize(R.dimen.view_help_prompt_edittext_fontsize)
                .toFloat()
        )
        setTextColor(context.getColor(R.color.primary))
        setHintTextColor(context.getColor(R.color.view_help_prompt_edittext_hint_color))
        background = context.getDrawable(R.drawable.background_view_help_prompt_edittext)
    }

    fun initView(keyName: String) {
        hint = keyName
        tag = keyName
    }
}