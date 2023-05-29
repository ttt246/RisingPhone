package com.matthaigh27.chatgptwrapper.widgets

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.models.common.HelpPromptModel

class HelpPromptWidget(context: Context, model: HelpPromptModel) : ConstraintLayout(context),
    View.OnClickListener {
    private lateinit var mListener: OnHelpPromptListener
    private lateinit var mLlPromptKeys: LinearLayout
    private lateinit var mTvKeysTitle: TextView

    private var mListEtPrompt: ArrayList<EditText>? = null

    val mModel: HelpPromptModel = model

    init {
        initView()
    }

    fun setOnClickListener(listener: OnHelpPromptListener) {
        mListener = listener
    }

    private fun initView() {
        inflate(context, R.layout.view_help_prompt, this)

        mLlPromptKeys = findViewById(R.id.ll_prompt_keys)
        mTvKeysTitle = findViewById(R.id.tv_keys_title)
        mTvKeysTitle.setText(mModel.name)

        findViewById<Button>(R.id.btn_ok).setOnClickListener(this)
        findViewById<Button>(R.id.btn_cancel).setOnClickListener(this)

        initPromptList()

    }

    private fun initPromptList() {
        if (mModel.tags!!.size > 0) {
            mLlPromptKeys.removeAllViews()
            mListEtPrompt = ArrayList()

            for (i in 0 until mModel.tags!!.size) {
                val etKey = HelpCommandEditText(context)
                etKey.initView(mModel.tags!![i])
                mLlPromptKeys.addView(etKey)
                mListEtPrompt!!.add(etKey)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_ok -> {
                outputCompletePrompt()
            }
            R.id.btn_cancel -> {
                mListener.onCancel()
            }
        }
    }

    private fun outputCompletePrompt() {
        var promptTemplate = mModel.prompt
        if(mModel.tags!!.size > 0) {
            mListEtPrompt!!.forEach { etKeyPrompt ->
                val prompt = etKeyPrompt.text.toString()
                if(prompt.isEmpty()) return
                promptTemplate = promptTemplate.replace(etKeyPrompt.tag.toString(), prompt)
            }
        }
        mListener.onSuccess(promptTemplate)
    }

    interface OnHelpPromptListener {
        fun onSuccess(prompt: String)
        fun onCancel()
    }
}