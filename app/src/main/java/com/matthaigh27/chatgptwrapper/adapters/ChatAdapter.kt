package com.matthaigh27.chatgptwrapper.adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.models.common.ContactModel
import com.matthaigh27.chatgptwrapper.models.common.HelpPromptModel
import com.matthaigh27.chatgptwrapper.models.viewmodels.ChatMessageModel
import com.matthaigh27.chatgptwrapper.utils.Constants.MSG_WIDGET_TYPE_HELP_PRMOPT
import com.matthaigh27.chatgptwrapper.utils.Constants.MSG_WIDGET_TYPE_SEARCH_CONTACT
import com.matthaigh27.chatgptwrapper.utils.Constants.MSG_WIDGET_TYPE_SMS
import com.matthaigh27.chatgptwrapper.utils.ImageHelper
import com.matthaigh27.chatgptwrapper.utils.Utils
import com.matthaigh27.chatgptwrapper.widgets.ContactDetailItem
import com.matthaigh27.chatgptwrapper.widgets.ContactDetailWidget
import com.matthaigh27.chatgptwrapper.widgets.HelpPromptWidget
import com.matthaigh27.chatgptwrapper.widgets.SearchContactWidget
import com.matthaigh27.chatgptwrapper.widgets.SmsEditorWidget
import org.json.JSONArray

class ChatAdapter(list: ArrayList<ChatMessageModel>, context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mChatModelList: ArrayList<ChatMessageModel> = ArrayList()
    private var mContext: Context

    private var mListener: MessageWidgetListener? = null
    var mOnSMSClickListener: ContactDetailItem.OnSMSClickListener? = null

    private val feedbackData = arrayOf(
        arrayOf(R.drawable.ic_thumb_up_disable, R.drawable.ic_thumb_down),
        arrayOf(R.drawable.ic_thumb_up_disable, R.drawable.ic_thumb_down_disable),
        arrayOf(R.drawable.ic_thumb_up, R.drawable.ic_thumb_down_disable),
    )

    init {
        mChatModelList = list
        mContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        /**
         * Inflate the custom layout and Return a new holder instance
         */
        return if (viewType == 0) {
            SendMessageViewHolder(
                inflater.inflate(
                    R.layout.item_container_sent_message, parent, false
                )
            )
        } else {
            ReceiveMessageViewHolder(
                inflater.inflate(
                    R.layout.item_container_received_message, parent, false
                )
            )
        }
    }

    /**
     * Involves populating data into the item through holder
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        /**
         * Get the data model based on position
         */
        val index = holder.adapterPosition
        val messageModel: ChatMessageModel = mChatModelList[index]
        if (messageModel.isMe) {
            setSentMessageData(holder as SendMessageViewHolder, messageModel)
        } else {
            setReceiveMessageData(holder as ReceiveMessageViewHolder, messageModel)
        }
    }

    private fun setSentMessageData(holder: SendMessageViewHolder, messageModel: ChatMessageModel) {
        /**
         * Set item views based on your views and data model
         */
        if (messageModel.message.isEmpty()) {
            holder.textMessage.visibility = View.GONE
        } else {
            holder.textMessage.text = messageModel.message
            holder.textMessage.visibility = View.VISIBLE
        }


        if (messageModel.image != null) {
            val radius = mContext.resources.getDimensionPixelSize(R.dimen.chat_message_item_radius)

            val originBmp = BitmapFactory.decodeByteArray(messageModel.image, 0, messageModel.image!!.size)
            val bmp = ImageHelper.getRoundedCornerBitmap(originBmp, radius)
            holder.imgMessage.visibility = View.VISIBLE
            holder.imgMessage.setImageBitmap(bmp)
            holder.imgMessage.setOnClickListener {
                onImageClick(originBmp)
            }
        } else {
            holder.imgMessage.visibility = View.GONE
        }

        if (messageModel.isWidget) {
            when (messageModel.widgetType) {
                MSG_WIDGET_TYPE_HELP_PRMOPT -> {
                    val model: HelpPromptModel =
                        HelpPromptModel.initModelWithString(messageModel.widgetDescription)
                    val helpPromptWidget = HelpPromptWidget(mContext, model)
                    val helpPromptListener = object : HelpPromptWidget.OnHelpPromptListener {
                        override fun onSuccess(prompt: String) {
                            mChatModelList[holder.adapterPosition].isWidget = false
                            holder.llMessageWidget.visibility = View.GONE
                            holder.llMessageWidget.removeAllViews()
                            mListener!!.sentHelpPrompt(prompt)
                        }

                        override fun onCancel() {
                            mChatModelList[holder.adapterPosition].isWidget = false
                            holder.llMessageWidget.visibility = View.GONE
                            holder.llMessageWidget.removeAllViews()
                            mListener!!.canceledHelpPrompt()
                        }
                    }
                    helpPromptWidget.setOnClickListener(helpPromptListener)
                    holder.llMessageWidget.addView(helpPromptWidget)
                    holder.llMessageWidget.visibility = View.VISIBLE
                }
            }
        } else {
            holder.llMessageWidget.visibility = View.GONE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setReceiveMessageData(holder: ReceiveMessageViewHolder, messageModel: ChatMessageModel) {
        /**
         * Set item views based on your views and data model
         */
        if (messageModel.message.isEmpty()) {
            holder.textMessage.visibility = View.GONE
        } else {
            holder.textMessage.text = messageModel.message
            holder.textMessage.visibility = View.VISIBLE
        }


        if (messageModel.image != null) {
            val radius = mContext.resources.getDimensionPixelSize(R.dimen.chat_message_item_radius)

            val originBmp = BitmapFactory.decodeByteArray(messageModel.image, 0, messageModel.image!!.size)
            val bmp = ImageHelper.getRoundedCornerBitmap(originBmp, radius)
            holder.imgMessage.visibility = View.VISIBLE
            holder.imgMessage.setImageBitmap(bmp)
            holder.imgMessage.setOnClickListener {
                onImageClick(originBmp)
            }
        } else {
            holder.imgMessage.visibility = View.GONE
        }

        holder.llFeedback.visibility = if (messageModel.visibleFeedback) {
            View.VISIBLE
        } else {
            View.GONE
        }

        setThumb(holder)

        holder.itemLayout.setOnLongClickListener {
            if (holder.llFeedback.visibility == View.VISIBLE) {
                holder.llFeedback.visibility = View.GONE
                mChatModelList[holder.adapterPosition].visibleFeedback = false
            } else {
                holder.llFeedback.visibility = View.VISIBLE
                mChatModelList[holder.adapterPosition].visibleFeedback = true
            }
            return@setOnLongClickListener true
        }

        holder.btnThumbUp.setOnClickListener {
            mChatModelList[holder.adapterPosition].feedback = 1
            setThumb(holder)

        }

        holder.btnThumbDown.setOnClickListener {
            mChatModelList[holder.adapterPosition].feedback = -1
            setThumb(holder)
        }

        if (messageModel.isWidget) {
            holder.llContactWidget.removeAllViews()
            when (messageModel.widgetType) {
                MSG_WIDGET_TYPE_SMS -> {
                    val smsWidget = SmsEditorWidget(mContext, null)
                    if(messageModel.widgetDescription.isNotEmpty()) {
                        smsWidget.setToUserName(messageModel.widgetDescription)
                    }
                    holder.llMessageWidget.addView(smsWidget)
                    holder.llMessageWidget.visibility = View.VISIBLE

                    val smsListener = object : SmsEditorWidget.OnClickListener {
                        override fun confirmSMS(phonenumber: String, message: String) {
                            mChatModelList[holder.adapterPosition].isWidget = false
                            holder.llMessageWidget.visibility = View.GONE
                            holder.llMessageWidget.removeAllViews()
                            mListener!!.sentSMS(phonenumber, message)
                        }

                        override fun cancelSMS() {
                            mChatModelList[holder.adapterPosition].isWidget = false
                            holder.llMessageWidget.visibility = View.GONE
                            holder.llMessageWidget.removeAllViews()
                            mListener!!.canceledSMS()
                        }
                    }

                    smsWidget.setOnClickListener(smsListener)
                }

                MSG_WIDGET_TYPE_SEARCH_CONTACT -> {
                    val contacts = Utils.instance.getContacts(mContext)

                    val contactIds = JSONArray(messageModel.widgetDescription)
                    for (i in 0 until contactIds.length()) {
                        val contactId = contactIds[i].toString()
                        val contact = Utils.instance.getContactModelById(contactId, contacts)

                        val searchContactWidget = SearchContactWidget(mContext, contact, null)
                        searchContactWidget.mSMSOnClickListener = mOnSMSClickListener
                        holder.llContactWidget.addView(searchContactWidget)
                    }
                    holder.llContactWidget.visibility = View.VISIBLE
                }
            }
        } else {
            holder.llMessageWidget.visibility = View.GONE
            holder.llContactWidget.visibility = View.GONE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setThumb(holder: ReceiveMessageViewHolder) {
        holder.btnThumbUp.setImageDrawable(
            mContext.getDrawable(
                feedbackData[mChatModelList[holder.adapterPosition].feedback + 1][0]
            )
        )
        holder.btnThumbDown.setImageDrawable(
            mContext.getDrawable(
                feedbackData[mChatModelList[holder.adapterPosition].feedback + 1][1]
            )
        )
    }

    /**
     * Returns the total count of items in the list
     */
    override fun getItemCount(): Int {
        return mChatModelList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (mChatModelList[position].isMe) 0 else 1
    }

    private fun onImageClick(bitmap: Bitmap) {
        val dialog = Dialog(mContext)
        dialog.setContentView(R.layout.view_full_image)
        val fullImage = dialog.findViewById(R.id.fullImage) as ImageView
        fullImage.setImageBitmap(bitmap)
        dialog.show()
    }

    inner class ReceiveMessageViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var textMessage: TextView
        var imgMessage: ImageView
        var llFeedback: LinearLayout
        var btnThumbUp: ImageView
        var btnThumbDown: ImageView
        var itemLayout: ConstraintLayout
        var llMessageWidget: LinearLayout
        var llContactWidget: LinearLayout

        init {
            textMessage = itemView.findViewById<View>(R.id.textMessage) as TextView
            imgMessage = itemView.findViewById<View>(R.id.imgMessage) as ImageView
            btnThumbUp = itemView.findViewById<View>(R.id.btn_thumb_up) as ImageView
            btnThumbDown = itemView.findViewById<View>(R.id.btn_thumb_down) as ImageView
            llFeedback = itemView.findViewById<View>(R.id.ll_feedback) as LinearLayout
            itemLayout = itemView.findViewById<View>(R.id.cl_receive_message) as ConstraintLayout
            llMessageWidget = itemView.findViewById<View>(R.id.ll_message_widget) as LinearLayout
            llContactWidget = itemView.findViewById<View>(R.id.ll_contacts_widget) as LinearLayout
        }
    }

    inner class SendMessageViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var textMessage: TextView
        var imgMessage: ImageView
        var itemLayout: ConstraintLayout
        var llMessageWidget: LinearLayout

        init {
            textMessage = itemView.findViewById<View>(R.id.textMessage) as TextView
            imgMessage = itemView.findViewById<View>(R.id.imgMessage) as ImageView
            itemLayout = itemView.findViewById<View>(R.id.cl_sent_message) as ConstraintLayout
            llMessageWidget = itemView.findViewById<View>(R.id.ll_message_widget) as LinearLayout
        }
    }

    interface MessageWidgetListener {
        fun sentSMS(phonenumber: String, message: String)
        fun canceledSMS()
        fun sentHelpPrompt(prompt: String)
        fun canceledHelpPrompt()
    }

    fun setMessageWidgetListener(listener: MessageWidgetListener) {
        mListener = listener
    }
}