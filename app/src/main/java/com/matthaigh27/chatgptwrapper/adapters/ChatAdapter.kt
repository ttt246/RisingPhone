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
import com.matthaigh27.chatgptwrapper.models.ChatModel
import com.matthaigh27.chatgptwrapper.utils.Consts.MSG_WIDGET_TYPE
import com.matthaigh27.chatgptwrapper.utils.ImageHelper
import com.matthaigh27.chatgptwrapper.widgets.SmsEditorWidget


class ChatAdapter(list: ArrayList<ChatModel>, context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mChatModelList: ArrayList<ChatModel> = ArrayList()
    private var mContext: Context

    var mListener: SendSMSListener? = null

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
        val contact: ChatModel = mChatModelList[index]
        if (contact.isMe) {
            setSentMessageData(holder as SendMessageViewHolder, contact)
        } else {
            setReceiveMessageData(holder as ReceiveMessageViewHolder, contact)
        }
    }

    fun setSentMessageData(holder: SendMessageViewHolder, contact: ChatModel) {
        /**
         * Set item views based on your views and data model
         */
        if (contact.message == "") {
            holder.textMessage.visibility = View.GONE
        } else {
            holder.textMessage.text = contact.message
            holder.textMessage.visibility = View.VISIBLE
        }


        if (contact.image != null) {
            val radius = mContext.resources.getDimensionPixelSize(R.dimen._8sdp)

            val originBmp = BitmapFactory.decodeByteArray(contact.image, 0, contact.image!!.size)
            val bmp = ImageHelper.getRoundedCornerBitmap(originBmp, radius)
            holder.imgMessage.visibility = View.VISIBLE
            holder.imgMessage.setImageBitmap(bmp)
            holder.imgMessage.setOnClickListener {
                onImageClick(originBmp)
            }
        } else {
            holder.imgMessage.visibility = View.GONE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setReceiveMessageData(holder: ReceiveMessageViewHolder, contact: ChatModel) {
        /**
         * Set item views based on your views and data model
         */
        if (contact.message == "") {
            holder.textMessage.visibility = View.GONE
        } else {
            holder.textMessage.text = contact.message
            holder.textMessage.visibility = View.VISIBLE
        }


        if (contact.image != null) {
            val radius = mContext.resources.getDimensionPixelSize(R.dimen._8sdp)

            val originBmp = BitmapFactory.decodeByteArray(contact.image, 0, contact.image!!.size)
            val bmp = ImageHelper.getRoundedCornerBitmap(originBmp, radius)
            holder.imgMessage.visibility = View.VISIBLE
            holder.imgMessage.setImageBitmap(bmp)
            holder.imgMessage.setOnClickListener {
                onImageClick(originBmp)
            }
        } else {
            holder.imgMessage.visibility = View.GONE
        }

        holder.llFeedback.visibility = if (contact.visibleFeedback) {
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

        if(contact.isWidget) {
            when(contact.widgetType) {
                MSG_WIDGET_TYPE -> {
                    val smsWidget = SmsEditorWidget(mContext, null)
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
                        }
                    }

                    smsWidget.setOnClickListener(smsListener)
                }
            }
        } else {
            holder.llMessageWidget.visibility = View.GONE
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
        dialog.setContentView(R.layout.full_image_layout)
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
        var llMessageWidget: FrameLayout

        init {
            textMessage = itemView.findViewById<View>(R.id.textMessage) as TextView
            imgMessage = itemView.findViewById<View>(R.id.imgMessage) as ImageView
            btnThumbUp = itemView.findViewById<View>(R.id.btn_thumb_up) as ImageView
            btnThumbDown = itemView.findViewById<View>(R.id.btn_thumb_down) as ImageView
            llFeedback = itemView.findViewById<View>(R.id.ll_feedback) as LinearLayout
            itemLayout = itemView.findViewById<View>(R.id.cl_receive_message) as ConstraintLayout
            llMessageWidget = itemView.findViewById<View>(R.id.ll_message_widget) as FrameLayout
        }
    }

    inner class SendMessageViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var textMessage: TextView
        var imgMessage: ImageView
        var itemLayout: ConstraintLayout

        init {
            textMessage = itemView.findViewById<View>(R.id.textMessage) as TextView
            imgMessage = itemView.findViewById<View>(R.id.imgMessage) as ImageView
            itemLayout = itemView.findViewById<View>(R.id.cl_sent_message) as ConstraintLayout
        }
    }

    interface SendSMSListener {
        fun sentSMS(phonenumber: String, message: String);
    }

    fun setSendSMSListener(listener: SendSMSListener) {
        mListener = listener
    }
}