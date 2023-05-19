package com.matthaigh27.chatgptwrapper.utils;

import com.matthaigh27.chatgptwrapper.MyApplication;

import okhttp3.MediaType;

/**
 * const variables
 */
public class Consts {
    /**
     * app names
     */
    public static String APP_BROWSER = "browser";
    public static String APP_ALERT = "alert";
    public static String APP_MESSAGE = "message";
    public static String APP_PROGRAM = "program";
    public static String APP_CONTENT = "content";
    public static String APP_URL = "url";
    public static String APP_IMAGE = "image";
    public static String APP_SMS = "sms";

    /**
     * message widget type
     */
    public static String MSG_WIDGET_TYPE = "SMS";

    /**
     * okhttp server url
     */

    public static String SERVER_URL = "https://smartphone.herokuapp.com/";
    public static long CUSTOM_TIMEOUT = 120;

    /**
     * ImagePickerType
     */
    public static String PICKERTYPE_IMAGE_UPLOAD = "image_upload";
    public static String PICKERTYPE_IMAGE_PICK = "image_picker";

    /**
     * for send OkHttp3Request with json format
     */
    public static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static String TAG = "risingandroid";

    /**
     * lading text
     */
    public static String LOADING_ASKING_TO_GPT = "Asking To GPT";
    public static String LOADING_UPLOADING_IAMGE = "Uploading Image";
    public static String LOADING_ANALYZING_IMAGE = "Analyzing Image";
    public static String LOADING_DOWNLOADING_IMAGE = "Downloading Image";
}
