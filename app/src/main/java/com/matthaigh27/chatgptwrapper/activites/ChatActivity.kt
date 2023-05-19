package com.matthaigh27.chatgptwrapper.activites

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.provider.ContactsContract
import android.provider.MediaStore
import android.telephony.SmsManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.matthaigh27.chatgptwrapper.MyApplication
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.adapters.ChatAdapter
import com.matthaigh27.chatgptwrapper.database.DatabaseHandler
import com.matthaigh27.chatgptwrapper.dialogs.ImagePickerDialog
import com.matthaigh27.chatgptwrapper.dialogs.ImagePickerDialog.OnPositiveButtonClickListener
import com.matthaigh27.chatgptwrapper.models.ChatModel
import com.matthaigh27.chatgptwrapper.models.ContactModel
import com.matthaigh27.chatgptwrapper.models.ImageTableModel
import com.matthaigh27.chatgptwrapper.services.api.HttpClient
import com.matthaigh27.chatgptwrapper.services.api.HttpRisingInterface
import com.matthaigh27.chatgptwrapper.utils.Consts.*
import com.matthaigh27.chatgptwrapper.utils.Utils
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


class ChatActivity : AppCompatActivity(), OnClickListener, HttpRisingInterface {

    /**
     * ui components for chatlist recyclerview
     */
    private lateinit var mRvChatList: RecyclerView
    private lateinit var mEtMessage: EditText

    /**
     * ui components for loading spinner
     */
    private lateinit var mViewOverlay: View
    private lateinit var mSpLoading: ImageView
    private lateinit var mTvLoading: TextView
    private lateinit var mLlLoading: LinearLayout

    /**
     * ui components for loading photo
     */
    private lateinit var mLlLoadPhoto: LinearLayout
    private lateinit var mIvLoadedPhoto: ImageView
    private lateinit var mBtnCancelLoadPhoto: TextView

    /**
     * adapter for chat recyclerview and arraylist for store chatting history
     */
    private var mMessageList: ArrayList<ChatModel> = ArrayList()
    private lateinit var mAdapter: ChatAdapter

    private var mSelectedImage: ByteArray? = null
    private var mSelectedImageName: String = ""

    /**
     * mImagePickerType is
     * 'image_uplaod' when user is going to upload image
     * 'image_picker' when user is going to pick image for prompting
     */
    private lateinit var mImagePickerDlg: ImagePickerDialog
    private var mImagePickerType: String = ""

    /**
     * HttpClient for restful apis
     */
    private lateinit var httpClient : HttpClient

    private val rotate = RotateAnimation(
        0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    )

    private val requestExternalStoragePermission = 1
    private val CONTACTS_PERMISSION_REQUEST = 2
    private val SMS_PERMISSION_REQUEST_CODE = 100

    private lateinit var mDatabaseHandler: DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        init()
    }

    private fun init() {
        initEnvironment()
        initValues()
        initView()
        initDatabase()

        getAllImagesFromStorage()
        requestContactsPermission()
        requestSmsPermission()
    }

    private fun initEnvironment() {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        rotate.duration = 3000
        rotate.repeatCount = Animation.INFINITE
        rotate.interpolator = LinearInterpolator()
    }

    private fun initValues() {
        httpClient = HttpClient(this)
    }

    private fun initView() {
        this.mAdapter = ChatAdapter(mMessageList, this)
        mRvChatList = findViewById<View>(R.id.chatRecycleView) as RecyclerView
        mRvChatList.adapter = mAdapter

        val sendSMSListener = object : ChatAdapter.SendSMSListener {
            override fun sentSMS(phonenumber: String, message: String) {
                sendSms(phonenumber, message)
            }
        }

        mAdapter.setSendSMSListener(sendSMSListener)

        val linearLayoutManager = LinearLayoutManager(this)
        mRvChatList.layoutManager = linearLayoutManager

        this.mEtMessage = findViewById(R.id.et_message)
        mEtMessage.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                addMessage(mEtMessage.text.toString(), true)
            }
            return@setOnKeyListener false
        }

        findViewById<View>(R.id.btn_send_message).setOnClickListener(this)
        findViewById<View>(R.id.btn_image_upload).setOnClickListener(this)
        findViewById<View>(R.id.btn_image_picker).setOnClickListener(this)

        mViewOverlay = findViewById(R.id.overlay)
        mSpLoading = findViewById(R.id.sp_loading)
        mTvLoading = findViewById(R.id.tv_loading)
        mLlLoading = findViewById(R.id.ll_loading)

        mLlLoadPhoto = findViewById(R.id.ll_load_photo)
        mIvLoadedPhoto = findViewById(R.id.iv_load_photo)
        mBtnCancelLoadPhoto = findViewById(R.id.btn_cancel_load_photo)
        mBtnCancelLoadPhoto.setOnClickListener(this)


        mImagePickerDlg = ImagePickerDialog(this@ChatActivity)
        initFilePickerDialog()
    }

    private fun initDatabase() {
        mDatabaseHandler = DatabaseHandler(this@ChatActivity)
    }

    private fun getAllImagesFromStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                requestExternalStoragePermission
            )
        } else {
            fetchImages()
        }
    }

    fun requestContactsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION_REQUEST)
        } else {
            getContacts()
        }
    }


    /**
     * set loading spinner visible
     */
    private fun setDisableActivity(enable: Boolean) {
        mEtMessage.isEnabled = enable
        findViewById<View>(R.id.btn_send_message).isEnabled = enable
        findViewById<View>(R.id.btn_image_upload).isEnabled = enable
        findViewById<View>(R.id.btn_image_picker).isEnabled = enable
    }

    /**
     * set loading spinner visible
     */
    private fun showLoading(visible: Boolean, text: String = "") {
        this@ChatActivity.runOnUiThread {
            setDisableActivity(!visible)

            if (visible) {
                mSpLoading.startAnimation(rotate)

                mViewOverlay.visibility = View.VISIBLE
                mLlLoading.visibility = View.VISIBLE

                mTvLoading.text = if (text == "") {
                    ""
                } else {
                    text
                }
            } else {
                mSpLoading.clearAnimation()

                mViewOverlay.visibility = View.GONE
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
                imageByteArray, 0, imageByteArray.size
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
        if (imageName == "" && imageDesc != "") {
            this@ChatActivity.runOnUiThread {
                addMessage(imageDesc, false)
            }
            return
        }

        showLoading(true, LOADING_DOWNLOADING_IMAGE)

        val imageNameToUpload = "images/$imageName"

        val storageReference = FirebaseStorage.getInstance().getReference(imageNameToUpload)
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            try {
                val image = Utils.instance.getBitmapFromURL(uri.toString())
                if(image == null)
                    showToast("can not get bitmap from url")
                val baos = ByteArrayOutputStream()
                image!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                mSelectedImage = baos.toByteArray()
            } catch (e: Exception) {
                showToast("cannnot get downloadurl from firebase store")
                e.printStackTrace()
            }

            this@ChatActivity.runOnUiThread {
                addMessage(imageDesc, false)
                showLoading(false)
            }
        }
        return
    }

    /**
     * resume firebase clouding message service when chatactivity is resumed
     */
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Resume")
        val filter = IntentFilter().apply { addAction("android.intent.action.MAIN") }
        registerReceiver(receiver, filter) // Register the receiver in onResume
    }

    /**
     * pause firebase clouding message service when chatactivity is paused
     */
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Stop")
        unregisterReceiver(receiver) // Unregister the receiver in onPause to avoid leaks
    }

    /**
     * add message to chat list
     * if users picked image from camera or gallery, send post request to `sendNotification` to ask generally
     * else do 'imageRelateness' to search image
     *
     * @param message message content for chat list
     * @param isMe this identify if this is sent by langchain server or users send message to server
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun addMessage(message: String, isMe: Boolean, isWidget: Boolean = false, widgetType: String = "") {
        if (message == "" && mSelectedImage == null) return

        mLlLoadPhoto.visibility = View.GONE

        val msg = ChatModel()
        msg.message = message
        msg.isMe = isMe
        msg.isWidget = isWidget
        msg.widgetType = widgetType

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
        if (mSelectedImageName != "") {
            msg.imageName = mSelectedImageName
            mSelectedImageName = ""
        }

        mMessageList.add(msg)
        mAdapter.notifyDataSetChanged()
        mEtMessage.setText("")
        mRvChatList.scrollTo(1000, -1000)
        mRvChatList.scrollToPosition(mMessageList.size - 1)

        if (isMe) {
            showLoading(true, LOADING_ASKING_TO_GPT)
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
                mImagePickerDlg.show()
            }
            R.id.btn_image_picker -> {
                mImagePickerType = PICKERTYPE_IMAGE_PICK
                mImagePickerDlg.show()
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
     * @param type if users are goingto upload image or pick image for query
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
        this@ChatActivity.runOnUiThread {
            Toast.makeText(
                this@ChatActivity, message, Toast.LENGTH_SHORT
            ).show()
        }
    }


    /**
     * this can show dialog with camera and gallery icon.
     * when you click camera icon, camera application runs and users can get an image by capturing using camera.
     * when you click gallery icon, users can select image in your storage.
     * A picked image converts into bytearray data and upload to firebase storage.
     */
    private fun initFilePickerDialog() {
        val myImplementation = object : OnPositiveButtonClickListener {
            override fun onPositiveBtnClick(isCamera: Boolean?) {
                if (isCamera == true) {
                    CoCo.with(this@ChatActivity).take(Utils.instance.createSDCardFile())
                        .start(object : CoCoAdapter<TakeResult>() {
                            override fun onSuccess(data: TakeResult) {
                                val byteArray : ByteArray? = Utils.instance.getBytesFromPath(data.savedFile!!.absolutePath)
                                if(byteArray == null)
                                    showToast("cannot get bytes from path")
                                pickedImage(
                                    byteArray!!,
                                    mImagePickerType
                                )
                            }

                            override fun onFailed(exception: Exception) {
                                super.onFailed(exception)
                                showToast("Fail to pick image. Please try again.")
                            }
                        })
                } else {
                    CoCo.with(this@ChatActivity).pick().range(Range.PICK_CONTENT)
                        .start(object : CoCoCallBack<PickResult> {

                            override fun onSuccess(data: PickResult) {
                                val byteArray : ByteArray? = Utils.instance.convertImageToByte(data.originUri)
                                if(byteArray == null)
                                    showToast("can not find such a file")
                                pickedImage(byteArray!!, mImagePickerType)
                            }

                            override fun onFailed(exception: Exception) {
                                showToast("Fail to pick image. Please try again.")
                            }
                        })
                }
            }
        }

        mImagePickerDlg.setOnClickListener(myImplementation)
        mImagePickerDlg.window!!.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
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
            if (json.has(APP_PROGRAM)) {
                when (json.getString(APP_PROGRAM)) {
                    APP_BROWSER -> {
                        this@ChatActivity.runOnUiThread {
                            addMessage(json.getString(APP_URL), false)
                        }
                        openBrowser(json.getString(APP_URL))
                        return
                    }
                    APP_ALERT -> {
                        MyApplication.appContext.showNotification(json.getString(APP_CONTENT))
                        return
                    }
                    APP_MESSAGE -> {
                        this@ChatActivity.runOnUiThread {
                            addMessage(json.getString(APP_CONTENT), false)
                        }
                        return
                    }
                    APP_IMAGE -> {
                        try {
                            val imageRes = JSONObject(json.getString(APP_CONTENT))

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
                    APP_SMS -> {
                        this@ChatActivity.runOnUiThread {
                            addMessage(json.getString(APP_CONTENT), false, true, MSG_WIDGET_TYPE)
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            this@ChatActivity.runOnUiThread {
                addMessage(msg, false)
            }
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
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                listOfImageUris.add(contentUri)
            }
        }

        return listOfImageUris
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestExternalStoragePermission -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    fetchImages()
                }
                return
            }
            CONTACTS_PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getContacts()
                }
            }
            SMS_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    // You can send an SMS now
                } else {
                    // Permission denied
                }
            }
        }
    }

    /**
     * @param path local path for converting to ByteArray
     * @return ByteArray data converted from image local path
     */
    private fun getBytesFromPath(path: String?): ByteArray? {
        try {
            val stream = FileInputStream(path)
            val byteArray = stream.readBytes()
            stream.close()
            return byteArray
        } catch (e: IOException) {
            showToast("cannot get bytes from path")
            e.printStackTrace()
        }
        return null
    }

    private fun fetchImages() {
        CoroutineScope(Dispatchers.IO).launch {
            val images = queryImagesFromExternalStorage(contentResolver)
            val originalImages = mDatabaseHandler.viewImages()
            images.forEach {
                var existFlag = false
                val path = getRealPathFromUri(this@ChatActivity, it)
                originalImages.forEach {
                    if (it.path == path) {
                        existFlag = true
                        return@forEach
                    }
                }
                if (!existFlag) {
                    val byteArray = getBytesFromPath(path)
                    val uuid = uploadImageToFirebaseStorage(byteArray!!)

                    Log.d(TAG, uuid.toString())
                    mDatabaseHandler.addImage(ImageTableModel("${uuid}", path!!))
                }
            }
        }
    }

    fun getRealPathFromUri(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    @SuppressLint("Range")
    fun getContacts() {
        val contacts = mutableListOf<ContactModel>()

        val contentResolver = contentResolver
        val contactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER)

        val cursor = contentResolver.query(contactsUri, projection, null, null, null)

        cursor?.let {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val contact = ContactModel(id, name, phoneNumber)
                contacts.add(contact)
            }
            cursor.close()
        }

        // Use the fetched contacts data
        for (contact in contacts) {
            Log.i("$TAG ContactInfo", "ID: ${contact.id}, Name: ${contact.name}, Phone Number: ${contact.phoneNumber}")
        }
    }

    fun sendSms(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        val parts = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
        showToast("Sent SMS")
    }

    fun requestSmsPermission() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_REQUEST_CODE)
        }
    }
}


