package com.matthaigh27.chatgptwrapper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.provider.MediaStore
import android.telephony.SmsManager
import android.util.Log
import android.view.KeyEvent
import android.view.View.OnClickListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.matthaigh27.chatgptwrapper.MyApplication
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.adapters.ChatAdapter
import com.matthaigh27.chatgptwrapper.database.MyDatabase
import com.matthaigh27.chatgptwrapper.database.entity.ImageEntity
import com.matthaigh27.chatgptwrapper.widgets.ImagePickerWidget
import com.matthaigh27.chatgptwrapper.widgets.ImagePickerWidget.OnPositiveButtonClickListener
import com.matthaigh27.chatgptwrapper.models.*
import com.matthaigh27.chatgptwrapper.models.common.HelpCommandModel
import com.matthaigh27.chatgptwrapper.models.common.HelpPromptModel
import com.matthaigh27.chatgptwrapper.models.viewmodels.ChatMessageModel
import com.matthaigh27.chatgptwrapper.services.api.HttpClient
import com.matthaigh27.chatgptwrapper.services.api.HttpRisingInterface
import com.matthaigh27.chatgptwrapper.utils.Constants.*
import com.matthaigh27.chatgptwrapper.utils.Utils
import com.matthaigh27.chatgptwrapper.widgets.ContactDetailItem
import com.qw.photo.CoCo
import com.qw.photo.callback.CoCoAdapter
import com.qw.photo.callback.CoCoCallBack
import com.qw.photo.constant.Range
import com.qw.photo.pojo.PickResult
import com.qw.photo.pojo.TakeResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment : Fragment(), OnClickListener, HttpRisingInterface {
    private lateinit var rootView: View
    private var mContext: Context? = null

    /** ui components for chatlist recyclerview */
    private lateinit var mRvChatList: RecyclerView
    private lateinit var mEtMessage: EditText

    /** ui components for loading spinner */
    private lateinit var mSpLoading: ImageView
    private lateinit var mTvLoading: TextView
    private lateinit var mLlLoading: LinearLayout

    /** ui components for loading photo */
    private lateinit var mLlLoadPhoto: LinearLayout
    private lateinit var mIvLoadedPhoto: ImageView
    private lateinit var mBtnCancelLoadPhoto: TextView

    /** adapter for chat recyclerview and arraylist for store chatting history */
    private var mMessageList: ArrayList<ChatMessageModel> = ArrayList()
    private lateinit var mAdapter: ChatAdapter

    /** when a user selects image by camera or gallery,
     * these two variables are used to save image source and name */
    private var mSelectedImage: ByteArray? = null
    private var mSelectedImageName: String = ""

    /**
     * mImagePickerType is
     * 'image_uplaod' when user is going to upload image
     * 'image_picker' when user is going to pick image for prompting
     */
    private lateinit var mImagePickerWidget: ImagePickerWidget
    private var mImagePickerType: String = ""

    /** HttpClient for restful apis */
    private lateinit var httpClient: HttpClient

    /** list of help prompt commands */
    private var mHelpPromptList: ArrayList<HelpPromptModel>? = null

    /** animation variable for loading spinner */
    private val rotate = RotateAnimation(
        0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    )

    /** room database handler for local database */
    private lateinit var mRoomDataHandler: MyDatabase

    /** status variable that checks if widget in chatting interface does exist */
    private var mIsExistWidget: Boolean = false

    /**
     * this is invoked when users click the message icon to send sms on contact detail dialog
     * that is shown when a user search contacts
     */
    private var mSMSOnClickListener: ContactDetailItem.OnSMSClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false)
        init()
        return rootView
    }

    private fun init() {
        initEnvironment()
        initValues()
        initView()
        initDatabase()

        trainImages()
        getAllPromptCommands()
    }


    private fun getAllPromptCommands() {
        showLoading(true, "Loading Help Prompt Data")
        httpClient.getALlHelpPromptCommands()
    }


    private fun initEnvironment() {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    private fun initValues() {
        mContext = context

        httpClient = HttpClient(this)

        rotate.duration = 3000
        rotate.repeatCount = Animation.INFINITE
        rotate.interpolator = LinearInterpolator()

        initSMSOnClickListener()
    }

    private fun initView() {
        this.mAdapter = ChatAdapter(mMessageList, mContext!!)
        this.mAdapter.mOnSMSClickListener = mSMSOnClickListener
        mRvChatList = rootView.findViewById<View>(R.id.chatRecycleView) as RecyclerView
        mRvChatList.adapter = mAdapter

        val sendSMSListener = object : ChatAdapter.MessageWidgetListener {
            override fun sentSMS(phonenumber: String, message: String) {
                mIsExistWidget = false
                sendSms(phonenumber, message)
                addMessage(
                    "You sent SMS with belowing content.\n\n " + "To: $phonenumber\n " + "Message: $message",
                    false
                )
            }

            override fun canceledSMS() {
                mIsExistWidget = false
                addMessage("You canceled SMS.", false)
            }

            override fun sentHelpPrompt(prompt: String) {
                mIsExistWidget = false
                addMessage(prompt, true)
            }

            override fun canceledHelpPrompt() {
                mIsExistWidget = false
                addMessage("You canceled help command.", false)
            }
        }

        mAdapter.setMessageWidgetListener(sendSMSListener)

        val linearLayoutManager = LinearLayoutManager(mContext)
        mRvChatList.layoutManager = linearLayoutManager

        this.mEtMessage = rootView.findViewById(R.id.et_message)
        mEtMessage.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                addMessage(mEtMessage.text.toString(), true)
            }
            return@setOnKeyListener false
        }

        rootView.findViewById<View>(R.id.btn_send_message).setOnClickListener(this)
        rootView.findViewById<View>(R.id.btn_image_upload).setOnClickListener(this)
        rootView.findViewById<View>(R.id.btn_image_picker).setOnClickListener(this)

        mSpLoading = rootView.findViewById(R.id.sp_loading)
        mTvLoading = rootView.findViewById(R.id.tv_loading)
        mLlLoading = rootView.findViewById(R.id.ll_loading)

        mLlLoadPhoto = rootView.findViewById(R.id.ll_load_photo)
        mIvLoadedPhoto = rootView.findViewById(R.id.iv_load_photo)
        mBtnCancelLoadPhoto = rootView.findViewById(R.id.btn_cancel_load_photo)
        mBtnCancelLoadPhoto.setOnClickListener(this)

        initImagePickerWidget()
    }

    private fun initDatabase() {
        mRoomDataHandler = MyDatabase.getDatabase(mContext!!)
    }

    private fun initSMSOnClickListener() {
        mSMSOnClickListener = object : ContactDetailItem.OnSMSClickListener {
            override fun onSMSClickListener(phoneNumber: String) {
                addMessage(
                    "SMS",
                    isMe = false,
                    isSend = false,
                    isWidget = true,
                    widgetType = MSG_WIDGET_TYPE_SMS,
                    widgetDescription = phoneNumber
                )
            }

            override fun onVoiceCallListener(phoneNumber: String, toName: String) {
                addMessage(
                    message = "You made a voice call to $toName($phoneNumber)",
                    isMe = false,
                    isSend = false
                )
            }
        }
    }

    /**
     * set loading spinner visible
     */
    private fun setDisableActivity(enable: Boolean) {
        runOnUIThread {
            mEtMessage.isEnabled = enable
            rootView.findViewById<View>(R.id.btn_send_message).isEnabled = enable
            rootView.findViewById<View>(R.id.btn_image_upload).isEnabled = enable
            rootView.findViewById<View>(R.id.btn_image_picker).isEnabled = enable
        }
    }

    /**
     * set loading spinner visible
     */
    private fun showLoading(visible: Boolean, text: String = "") {
        runOnUIThread {
            setDisableActivity(!visible)

            if (visible) {
                mSpLoading.startAnimation(rotate)
                mLlLoading.visibility = View.VISIBLE
                mTvLoading.text = text
            } else {
                mSpLoading.clearAnimation()
                mLlLoading.visibility = View.GONE
            }
        }
    }

    /**
     * show overlay when you picked image and users can cancel to load photo if users want
     */
    private fun showLoadPhotoOverlay(imageByteArray: ByteArray) {
        mLlLoadPhoto.visibility = View.VISIBLE

        mIvLoadedPhoto.setImageBitmap(
            BitmapFactory.decodeByteArray(
                /* data = */ imageByteArray, /* offset = */ 0, /* length = */ imageByteArray.size
            )
        )
    }

    /**
     * Bind broadcast sent by MessageService
     */
    private val receiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Receive")
        }
    }


    /**
     * get download url from firebase imagename in response and generate download url from the imagename
     * With getBitmapFromURL method, get bitmap of the image and set it to mSelectedImage to display it to chatlist
     * Finally, run addmessage and insert it to chat recyclerview
     *
     * @param imageName the firebase store imagename
     */
    private fun getImageResponse(imageName: String, imageDesc: String) {
        if (imageName.isEmpty() && imageDesc.isNotEmpty()) {
            addMessage(message = imageDesc, isMe = false)
            return
        }

        showLoading(visible = true, text = LOADING_DOWNLOADING_IMAGE)

        val imageNameToUpload = "images/$imageName"

        val storageReference = FirebaseStorage.getInstance().getReference(imageNameToUpload)
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            try {
                val image = Utils.instance.getBitmapFromURL(uri.toString())
                if (image == null) showToast("can not get bitmap from url")
                val byteArrayOutputStream = ByteArrayOutputStream()

                val isSuccess =
                    image!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                if (!isSuccess) {
                    showToast("Fail to compress image")
                }
                mSelectedImage = byteArrayOutputStream.toByteArray()
            } catch (e: Exception) {
                showToast("cannot get download url from firebase store")
                e.printStackTrace()
            }

            addMessage(message = imageDesc, isMe = false)
            showLoading(visible = false)
        }
        return
    }

    override fun onResume() {
        super.onResume()
        updateChangedUserData()
    }

    private fun updateChangedUserData() {
        trainContacts()
    }

    /**
     * Open Help Command Widget with analysing string command
     */
    private fun openHelpCommandWidget(strCommand: String) {
        try {
            val command: HelpCommandModel = Utils.instance.getHelpCommandFromStr(strCommand)
            if (command.isMainCommand()) {
                when (command.mainCommandName) {
                    "sms" -> {
                        addMessage(
                            message = "SMS",
                            isMe = false,
                            isSend = false,
                            isWidget = true,
                            widgetType = MSG_WIDGET_TYPE_SMS
                        )
                    }

                    else -> {
                        mHelpPromptList!!.forEach { model ->
                            if (model.name == command.mainCommandName) {
                                addMessage(
                                    "Help Prompt Command",
                                    isMe = true,
                                    isSend = false,
                                    isWidget = true,
                                    widgetType = MSG_WIDGET_TYPE_HELP_PRMOPT,
                                    widgetDescription = model.toString()
                                )
                                return
                            }
                        }
                        addMessage(
                            message = "No such command name exists.", isMe = false, isSend = false
                        )
                    }
                }
            } else {
                if (command.assistCommandName == "all") {
                    val usage =
                        "usage:\n" + "- help command: /help [command name]\n" + "- prompt command: /<command name>\n\n"
                    var strHelpList = "help prompt commands:"
                    mHelpPromptList!!.forEach { model ->
                        strHelpList += "\n- " + model.name
                    }

                    addMessage(message = usage + strHelpList, isMe = false, isSend = false)
                } else {
                    var strHelpDesc = ""
                    mHelpPromptList!!.forEach { model ->
                        if (model.name == command.assistCommandName) {
                            var strTags = ""
                            model.tags!!.forEach { tag ->
                                strTags += " $tag"
                            }
                            strHelpDesc = "description: " + model.description + "\ntags:" + strTags
                        }
                    }
                    if (strHelpDesc.isEmpty()) addMessage(
                        message = "No such command name exists.", isMe = false, isSend = false
                    )
                    else addMessage(message = strHelpDesc, isMe = false, isSend = false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(e.message.toString())
        }
    }

    /**
     * add message to chat list
     * if users picked image from camera or gallery, send post request to `sendNotification` to ask generally
     * else do 'imageRelateness' to search image
     *
     * @param message message content for chat list
     * @param isMe this identify if this is sent by langchain server or users send message to server
     * @param isSend is boolean that checks if you send request to server
     * @param isWidget is boolean that checks if message item has widget
     * @param widgetType is type of Widget ex: SMS, HELP_COMMAND, etc
     * @param widgetDescription is string that saves information for widget
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun addMessage(
        message: String,
        isMe: Boolean,
        isSend: Boolean = true,
        isWidget: Boolean = false,
        widgetType: String = "",
        widgetDescription: String = ""
    ) {
        if ((message.isEmpty() && mSelectedImage == null) || mIsExistWidget) return
        if (isWidget) {
            if (widgetType != MSG_WIDGET_TYPE_SEARCH_CONTACT) {
                mIsExistWidget = true
            }
        }

        val msg = ChatMessageModel()
        msg.message = message
        msg.isMe = isMe
        msg.isWidget = isWidget
        msg.widgetType = widgetType
        msg.widgetDescription = widgetDescription

        /**
         * if users picked some image from camera or gallery, add the image to chatting message
         */
        if (mSelectedImage != null) {
            msg.image = mSelectedImage
            mSelectedImage = null
        }

        /**
         * if users picked some image from camera or gallery, the image upload to firebase store
         * mSelectedImageName is uuid created uploading to firebase store
         */
        if (mSelectedImageName.isNotEmpty()) {
            msg.imageName = mSelectedImageName
            mSelectedImageName = ""
        }

        runOnUIThread {
            mLlLoadPhoto.visibility = View.GONE

            mMessageList.add(msg)
            mAdapter.notifyDataSetChanged()
            mEtMessage.setText("")
            mRvChatList.scrollTo(/* x = */ 1000, /* y = */ -1000)
            mRvChatList.scrollToPosition(mMessageList.size - 1)
        }

        if ((message.isNotEmpty() && message.first() == '/') && isMe) {
            openHelpCommandWidget(message)
            return
        }

        if (isMe) {
            if (!isSend) {
                return
            }

            showLoading(visible = true, text = LOADING_ASKING_TO_GPT)
            if (msg.image != null) {
                httpClient.callImageRelatedness(msg.imageName)
            } else {
                httpClient.callSendNotification(message)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_send_message -> {
                addMessage(mEtMessage.text.toString(), true)
            }

            R.id.btn_image_upload -> {
                mImagePickerType = PICKERTYPE_IMAGE_UPLOAD
                if(rootView.findViewById<View>(R.id.ll_toolbar).visibility == View.VISIBLE)
                    hideSlidingWidget()
                else
                    showSlidingWidget()
            }

            R.id.btn_image_picker -> {
                mImagePickerType = PICKERTYPE_IMAGE_PICK
                if(rootView.findViewById<View>(R.id.ll_toolbar).visibility == View.VISIBLE)
                    hideSlidingWidget()
                else
                    showSlidingWidget()
            }

            R.id.btn_cancel_load_photo -> {
                mLlLoadPhoto.visibility = View.GONE
                mSelectedImageName = ""
                mSelectedImage = null
            }
        }
    }

    /**
     * open browser with url
     * @param url to open with browser
     */
    private fun openBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(Intent.createChooser(intent, "Browse with"))
    }

    /**
     * calls when finish picking image
     *
     * @param imageByteData is bytearray of picked image
     * @param type if users are going to upload image or pick image for query
     */
    private fun pickedImage(imageByteData: ByteArray, type: String) {
        if (type == "image_upload") {
            uploadImageToFirebaseStorage(imageByteData)
        } else {
            uploadSearchImage(imageByteData)
        }
    }

    /**
     * toast message is invoked when error happens
     */
    private fun showToast(message: String) {
        runOnUIThread {
            Toast.makeText(
                mContext, message, Toast.LENGTH_SHORT
            ).show()
        }
    }


    /**
     * this can show dialog with camera and gallery icon.
     * when you click camera icon, camera application runs and users can get an image by capturing using camera.
     * when you click gallery icon, users can select image in your storage.
     * A picked image converts into bytearray data and upload to firebase storage.
     */
    private fun initImagePickerWidget() {
        mImagePickerWidget = ImagePickerWidget(mContext!!)

        val myImplementation = object : OnPositiveButtonClickListener {
            override fun onPositiveBtnClick(isCamera: Boolean?) {
                if (isCamera == true) {
                    CoCo.with(activity!!).take(Utils.instance.createSDCardFile())
                        .start(object : CoCoAdapter<TakeResult>() {
                            override fun onSuccess(data: TakeResult) {
                                val byteArray: ByteArray =
                                    Utils.instance.getBytesFromPath(data.savedFile!!.absolutePath)
                                pickedImage(
                                    byteArray, mImagePickerType
                                )
                            }

                            override fun onFailed(exception: Exception) {
                                super.onFailed(exception)
                                showToast("Fail to pick image. Please try again.")
                            }
                        })
                } else {
                    CoCo.with(activity!!).pick().range(Range.PICK_CONTENT)
                        .start(object : CoCoCallBack<PickResult> {

                            override fun onSuccess(data: PickResult) {
                                val byteArray: ByteArray? =
                                    Utils.instance.convertImageToByte(data.originUri)
                                if (byteArray == null) showToast("can not find such a file")
                                pickedImage(byteArray!!, mImagePickerType)
                            }

                            override fun onFailed(exception: Exception) {
                                showToast("Fail to pick image. Please try again.")
                            }
                        })
                }
            }
        }

        mImagePickerWidget.setOnClickListener(myImplementation)

        val slidingWidget = rootView.findViewById<LinearLayout>(R.id.ll_toolbar)
        slidingWidget.addView(mImagePickerWidget)
    }

    private fun uploadSearchImage(imageByteArray: ByteArray) {
        showLoading(true, LOADING_ANALYZING_IMAGE)
        val storageRef = FirebaseStorage.getInstance().reference
        val uuid = UUID.randomUUID()
        val imageName = "images/${uuid}"
        val imageRef = storageRef.child(imageName)

        val uploadTask = imageRef.putBytes(imageByteArray)
        uploadTask.addOnFailureListener {
            showLoading(false)
        }.addOnSuccessListener {
            Log.d(TAG, "Success upload to firebase storage")
            showLoading(false)

            mSelectedImageName = "$uuid"
            mSelectedImage = imageByteArray

            showLoadPhotoOverlay(imageByteArray)
        }
    }

    /**
     * @param imageByteArray ByteArray data for image to upload to firebase storage
     */
    private fun uploadImageToFirebaseStorage(imageByteArray: ByteArray) {
        showLoading(true, LOADING_UPLOADING_IAMGE)
        val storageRef = FirebaseStorage.getInstance().reference
        val uuid = UUID.randomUUID()
        val imageName = "images/${uuid}"
        val imageRef = storageRef.child(imageName)

        val uploadTask = imageRef.putBytes(imageByteArray)
        uploadTask.addOnFailureListener {
            showLoading(false)
        }.addOnSuccessListener {
            Log.d(TAG, "Success upload to firebase storage")

            showLoading(false)
            httpClient.callImageUpload("$uuid")
        }
    }

    override fun onSuccessResult(msg: String) {
        showLoading(false)
        try {
            val json = JSONObject(msg)
            if (json.has(RESPONSE_TYPE_PROGRAM)) {
                when (json.getString(RESPONSE_TYPE_PROGRAM)) {
                    RESPONSE_TYPE_BROWSER -> {
                        addMessage(json.getString(RESPONSE_TYPE_URL), false)
                        openBrowser(json.getString(RESPONSE_TYPE_URL))
                        return
                    }

                    RESPONSE_TYPE_ALERT -> {
                        MyApplication.appContext.showNotification(
                            json.getString(
                                RESPONSE_TYPE_CONTENT
                            )
                        )
                        return
                    }

                    RESPONSE_TYPE_MESSAGE -> {
                        addMessage(json.getString(RESPONSE_TYPE_CONTENT), false)
                        return
                    }

                    RESPONSE_TYPE_IMAGE -> {
                        try {
                            val imageRes = JSONObject(json.getString(RESPONSE_TYPE_CONTENT))

                            val imageName = if (imageRes.has("image_name")) {
                                imageRes["image_name"] as String
                            } else {
                                ""
                            }

                            val imageDesc = if (imageRes.has("image_desc")) {
                                imageRes["image_desc"] as String
                            } else {
                                ""
                            }

                            getImageResponse(imageName, imageDesc)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return
                    }

                    RESPONSE_TYPE_SMS -> {
                        addMessage(
                            json.getString(RESPONSE_TYPE_CONTENT),
                            false,
                            isSend = false,
                            isWidget = true,
                            widgetType = MSG_WIDGET_TYPE_SMS
                        )
                    }

                    RESPONSE_TYPE_HELP_COMMAND -> {
                        try {
                            mHelpPromptList = Utils.instance.getHelpCommandListFromJsonString(
                                json.getString(RESPONSE_TYPE_CONTENT)
                            )
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            showToast("JSON Error occured")
                        }
                    }

                    RESPONSE_TYPE_CONTACT -> {
                        try {
                            addMessage(
                                message = "Contacts that you are looking for.",
                                isMe = false,
                                isSend = false,
                                isWidget = true,
                                widgetType = MSG_WIDGET_TYPE_SEARCH_CONTACT,
                                widgetDescription = json.getString(RESPONSE_TYPE_CONTENT)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            addMessage(msg, false)
        }
    }

    override fun onFailureResult(msg: String) {
        showLoading(false)

        showToast(msg)
    }

    private fun queryImagesFromExternalStorage(contentResolver: ContentResolver): ArrayList<Uri> {
        val listOfImageUris = ArrayList<Uri>()

        val projection = arrayOf(MediaStore.Images.Media._ID)

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()
                )
                listOfImageUris.add(contentUri)
            }
        }
        return listOfImageUris
    }

    private fun trainImages() {
        CoroutineScope(Dispatchers.IO).launch {
            val images = queryImagesFromExternalStorage(requireContext().contentResolver)
            val originalImages = mRoomDataHandler.imageDao().getAllImages()

            images.forEach { uri ->
                var isExist = false
                val path = Utils.instance.getRealPathFromUri(requireContext(), uri)
                for (i in originalImages.indices) {
                    val entity: ImageEntity = originalImages[i]
                    if (entity.path == path) {
                        isExist = true
                        break
                    }
                }
                if (!isExist) {
                    val byteArray = Utils.instance.getBytesFromPath(path)
                    val uuid = uploadImageToFirebaseStorage(byteArray)

                    if (path != null)
                        mRoomDataHandler.imageDao().insertImage(ImageEntity(0, path, "$uuid"))
                }
            }
        }
    }

    private fun trainContacts() {
        showLoading(true, "Train Contacts")
        val contacts = Utils.instance.getContacts(mContext!!)
        CoroutineScope(Dispatchers.Main).launch {
            val changedContacts = Utils.instance.getChangedContacts(contacts, mRoomDataHandler)
            httpClient.trainContacts(changedContacts)
        }
    }

    fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(/* destinationAddress = */ phoneNumber, /* scAddress = */
                null, /* parts = */
                parts, /* sentIntents = */
                null, /* deliveryIntents = */
                null)
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun runOnUIThread(action: () -> Unit) {
        requireActivity().runOnUiThread {
            action()
        }
    }

    private fun showSlidingWidget() {
        val slidingWidget = rootView.findViewById<View>(R.id.ll_toolbar)

        val dy = slidingWidget.measuredHeight.toFloat()
        slidingWidget.visibility = View.VISIBLE

        val anim = TranslateAnimation(0f, 0f, dy, 0f).apply {
            duration = 150 // Set the animation duration, e.g., 300ms
            interpolator = AccelerateDecelerateInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    slidingWidget.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {}

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }

        slidingWidget.startAnimation(anim)
    }

    private fun hideSlidingWidget() {
        val slidingWidget = rootView.findViewById<View>(R.id.ll_toolbar)

        val anim = AlphaAnimation(1f, 0f).apply {
            duration = 100 // Set the animation duration, e.g., 300ms
            interpolator = AccelerateDecelerateInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    slidingWidget.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }

        slidingWidget.startAnimation(anim)
    }
}

