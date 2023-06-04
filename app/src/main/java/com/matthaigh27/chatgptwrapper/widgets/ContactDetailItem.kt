package com.matthaigh27.chatgptwrapper.widgets

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.VideoProfile
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.models.common.ContactModel

class ContactDetailItem(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), View.OnClickListener {
    private lateinit var mTvPhoneNumber: TextView
    private lateinit var mTvPhoneType: TextView
    private var mPhoneNumber: String = ""
    private var mUserName: String = ""

    private var mContext = context
    private var mListener: OnSMSClickListener? = null
    private var mContactDetailVisibilityListener: OnContactDetailVisibilityListener? = null

    init {
        initView()
    }

    fun setOnSMSClickListener(listener: OnSMSClickListener) {
        mListener = listener
    }

    fun setOnContactDetailVisibilityListener(listener: OnContactDetailVisibilityListener) {
        mContactDetailVisibilityListener = listener
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.item_contact_detail, this, true)

        mTvPhoneNumber = findViewById(R.id.tv_phone_number)
        mTvPhoneType = findViewById(R.id.tv_phone_type)

        findViewById<ImageView>(R.id.btn_voice_call).setOnClickListener(this)
        findViewById<ImageView>(R.id.btn_send_message).setOnClickListener(this)
    }

    fun setContactDetailItemInfo(phoneNumber: String, username: String) {
        mPhoneNumber = phoneNumber
        mUserName = username
        mTvPhoneNumber.text = phoneNumber
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.btn_voice_call -> {
                mContactDetailVisibilityListener!!.invisible()
                mListener!!.onVoiceCallListener(mPhoneNumber, mUserName)
                doVoiceCall(mPhoneNumber)
            }

            R.id.btn_send_message -> {
                mListener!!.onSMSClickListener(mPhoneNumber)
                mContactDetailVisibilityListener!!.invisible()
            }
        }
    }

    private fun doVoiceCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        mContext.startActivity(callIntent)
        return
    }

    fun openMessagingApp(phoneNumber: String, message: String) {
        val uri = Uri.parse("sms:$phoneNumber")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            putExtra("sms_body", message)
        }
        mContext.startActivity(intent)
    }

    private fun doVideoCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        callIntent.putExtra(
            "android.telecom.extra.START_CALL_WITH_VIDEO_STATE",
            VideoProfile.STATE_BIDIRECTIONAL
        )
        val hasCamera =
            context.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ?: false
        val hasTelephony =
            context.packageManager?.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) ?: false
        val canMakeVideoCalls = hasCamera && hasTelephony

        if (canMakeVideoCalls) {
            context.startActivity(callIntent)
        }
    }

    interface OnSMSClickListener {
        fun onSMSClickListener(phoneNumber: String)
        fun onVoiceCallListener(phoneNumber: String, toName: String)
    }

    interface OnContactDetailVisibilityListener {
        fun invisible()
    }
}