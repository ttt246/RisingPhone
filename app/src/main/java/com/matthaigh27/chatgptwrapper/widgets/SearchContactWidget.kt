package com.matthaigh27.chatgptwrapper.widgets

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.models.common.ContactModel
import com.matthaigh27.chatgptwrapper.utils.Utils
import de.hdodenhof.circleimageview.CircleImageView


class SearchContactWidget(
    context: Context, cotactModel: ContactModel, attrs: AttributeSet?
) : ConstraintLayout(context, attrs), View.OnClickListener {

    private val mContext: Context

    private val civInfoAvatar: CircleImageView
    private val tvInfoName: TextView

    private var mContact:ContactModel = cotactModel
    var mSMSOnClickListener: ContactDetailItem.OnSMSClickListener? = null

    init {
        inflate(context, R.layout.view_search_contact, this)
        mContext = context

        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        civInfoAvatar = findViewById(R.id.civ_avatar)
        tvInfoName = findViewById(R.id.tv_info_name)

        tvInfoName.text = mContact.name
        Utils.instance.setContactAvatar(mContact.id.toLong(), mContext, civInfoAvatar)

        this.setOnClickListener(this)
    }



    private fun showContactDetailView() {
        val bottomSheetDialog = ContactDetailWidget(mContext, mContact, mSMSOnClickListener!!)
        bottomSheetDialog.show()
    }

    override fun onClick(view: View?) {
        showContactDetailView()
    }
}