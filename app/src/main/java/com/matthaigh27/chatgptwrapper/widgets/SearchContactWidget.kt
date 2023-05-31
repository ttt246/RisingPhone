package com.matthaigh27.chatgptwrapper.widgets

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.VideoProfile
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.matthaigh27.chatgptwrapper.R
import de.hdodenhof.circleimageview.CircleImageView

class SearchContactWidget(
    context: Context, attrs: AttributeSet?
) : ConstraintLayout(context, attrs), View.OnClickListener {

    private val mContext: Context

    private val civInfoAvatar: CircleImageView
    private val tvInfoName: TextView
    private val tvInfoPhoneNumber: TextView

    private var mListener: SearchContactListener? = null

    init {
        inflate(context, R.layout.view_search_contact, this)
        mContext = context

        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        civInfoAvatar = findViewById(R.id.civ_avatar)
        tvInfoName = findViewById(R.id.tv_info_name)
        tvInfoPhoneNumber = findViewById(R.id.tv_info_phone_number)

        findViewById<AppCompatImageView>(R.id.btn_video_call).setOnClickListener(this)
        findViewById<AppCompatImageView>(R.id.btn_voice_call).setOnClickListener(this)
        findViewById<AppCompatImageView>(R.id.btn_message).setOnClickListener(this)
        findViewById<AppCompatImageView>(R.id.btn_goto_contact).setOnClickListener(this)
    }

    private fun setAvatarImage(imageUri: Uri) {
        Glide.with(this)
            .load(imageUri)
            .placeholder(R.drawable.img_test)
            .error(R.drawable.img_test)
            .into(civInfoAvatar)
    }

    fun setSearchContactListener(listener: SearchContactListener) {
        mListener = listener
    }

    interface SearchContactListener {
        fun invokedVoiceCall();
        fun invokedVideoCall();
        fun invokedMessage();
        fun invokedGoToContact();
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.btn_video_call -> {
                mListener!!.invokedVideoCall()
            }
            R.id.btn_voice_call -> {
                mListener!!.invokedVoiceCall()
            }
            R.id.btn_message -> {
                mListener!!.invokedMessage()
            }
            R.id.btn_goto_contact -> {
                mListener!!.invokedGoToContact()
            }
        }
    }

    private fun doVoiceCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber))
        mContext.startActivity(callIntent)
        return
    }

    private fun doVideoCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber))
        callIntent.putExtra("android.telecom.extra.START_CALL_WITH_VIDEO_STATE", VideoProfile.STATE_BIDIRECTIONAL)
        val hasCamera = mContext.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ?: false
        val hasTelephony = mContext.packageManager?.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) ?: false
        val canMakeVideoCalls = hasCamera && hasTelephony

        if (canMakeVideoCalls) {
            mContext.startActivity(callIntent)
        }
    }

    private fun goToContactEditor(contactId: String) {
        val editIntent = Intent(Intent.ACTION_EDIT)
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong())
        editIntent.setData(contactUri)
        mContext.startActivity(editIntent)
    }
}