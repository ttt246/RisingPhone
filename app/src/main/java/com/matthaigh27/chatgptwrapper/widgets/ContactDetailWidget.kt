package com.matthaigh27.chatgptwrapper.widgets

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.models.common.ContactModel
import com.matthaigh27.chatgptwrapper.utils.Utils

class ContactDetailWidget(
    context: Context, contactModel: ContactModel, smsClickListener: ContactDetailItem.OnSMSClickListener
) : BottomSheetDialog(context), View.OnClickListener {

    private lateinit var mAvatar: ImageView
    private lateinit var mDisplayName: TextView
    private lateinit var mBtnEditContact: ImageView
    private lateinit var mLlPhones: LinearLayout

    private var mContactModel = contactModel

    private var mOnSMSClickListener: ContactDetailItem.OnSMSClickListener = smsClickListener

    init {
        initView()
    }


    private fun initView() {
        setContentView(R.layout.view_contact_detail)

        mDisplayName = findViewById(R.id.tv_displayname)!!
        mBtnEditContact = findViewById(R.id.btn_edit_contact)!!
        mLlPhones = findViewById(R.id.ll_contacts)!!

        mLlPhones.removeAllViews()
        mDisplayName.text = mContactModel.name
        mContactModel.phoneList!!.forEach { phoneNumber ->
            val contactDetailItem = ContactDetailItem(context)
            contactDetailItem.setContactDetailItemInfo(phoneNumber, mContactModel.name)
            contactDetailItem.setOnSMSClickListener(mOnSMSClickListener)
            contactDetailItem.setOnContactDetailVisibilityListener(object:
                ContactDetailItem.OnContactDetailVisibilityListener {
                override fun invisible() {
                    this@ContactDetailWidget.dismiss()
                }
            })
            mLlPhones.addView(contactDetailItem)
        }

        mBtnEditContact.setOnClickListener(this)
        mAvatar = findViewById(R.id.iv_avatar)!!
        Utils.instance.setContactAvatar(mContactModel.id.toLong(), context, mAvatar)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.btn_edit_contact -> {
                goToContactEditor(mContactModel.id)
            }
            R.id.btn_send_message -> {
            }
        }
    }

    private fun goToContactEditor(contactId: String) {
        val editIntent = Intent(Intent.ACTION_EDIT)
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong())
        editIntent.setData(contactUri)
        context.startActivity(editIntent)
    }
}