package com.matthaigh27.chatgptwrapper.utils;

import okhttp3.MediaType;

/**
 * const variables
 */
public class Constants {
    /**
     * app names
     */
    public static String RESPONSE_TYPE_BROWSER = "browser";
    public static String RESPONSE_TYPE_ALERT = "alert";
    public static String RESPONSE_TYPE_MESSAGE = "message";
    public static String RESPONSE_TYPE_PROGRAM = "program";
    public static String RESPONSE_TYPE_CONTENT = "content";
    public static String RESPONSE_TYPE_URL = "url";
    public static String RESPONSE_TYPE_IMAGE = "image";
    public static String RESPONSE_TYPE_HELP_COMMAND = "help_command";
    public static String RESPONSE_TYPE_SMS = "sms";

    /**
     * message widget type
     */
    public static String MSG_WIDGET_TYPE_SMS = "SMS";
    public static String MSG_WIDGET_TYPE_HELP_PRMOPT = "HELP_PROMPT";

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
    public static String POST = "post";
    public static String GET = "get";

    public static String TAG = "risingandroid";

    /**
     * lading text
     */
    public static String LOADING_ASKING_TO_GPT = "Asking To GPT";
    public static String LOADING_UPLOADING_IAMGE = "Uploading Image";
    public static String LOADING_ANALYZING_IMAGE = "Analyzing Image";
    public static String LOADING_DOWNLOADING_IMAGE = "Downloading Image";

    public static Integer REQUEST_PERMISSION_CODE = 1;

    public static String HELP_COMMAND_ERROR_NO_MAIN = "no main command";
    public static String HELP_COMMAND_ERROR_NO_INVALID_FORMAT = "Invalid Command Format";
    public static String HELP_COMMAND = "help";
    public static String HELP_COMMAND_ALL = "all";

    public static String ERROR_MSG_JSON = "JSON Invalid Format";
}
