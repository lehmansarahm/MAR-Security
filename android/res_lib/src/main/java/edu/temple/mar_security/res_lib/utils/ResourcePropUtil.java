package edu.temple.mar_security.res_lib.utils;

import android.content.Context;

import edu.temple.mar_security.res_lib.R;

public class ResourcePropUtil {

    public static final String PACKAGE_NAME_SERVICES (Context context) {
        return context.getResources().getString(R.string.package_name_services);
    }

    public static final String PACKAGE_NAME_ANNOTATOR(Context context) {
        return "edu.temple.gtc_annotator";
    }

    // --------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------

    public static final String CHANNEL_ID(Context context) {
        return context.getResources().getString(R.string.channel_id);
    }

    public static final String CHANNEL_NAME(Context context) {
        return context.getResources().getString(R.string.channel_name);
    }

    public static final String CHANNEL_DESCRIPTION(Context context) {
        return context.getResources().getString(R.string.channel_description);
    }

    // --------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------

    public static final String SERVICE_NAME_COMMUNICATOR(Context context) {
        return context.getResources().getString(R.string.service_class_communicator);
    }

    public static final String SERVICE_NAME_RESOURCE_LOGGER(Context context) {
        return context.getResources().getString(R.string.service_class_resource_logger);
    }

    public static final String SERVICE_NAME_FRAME_LOGGER(Context context) {
        return context.getResources().getString(R.string.service_class_frame_logger);
    }

    // --------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------

    public static final String SERVICE_TEXT_START(Context context) {
        return context.getResources().getString(R.string.start_services_text);
    }
    public static final String SERVICE_TEXT_STOP(Context context) {
        return context.getResources().getString(R.string.stop_services_text);
    }

    // --------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------

    public static final String INTENT_EXTRA_PID(Context context) {
        return context.getResources().getString(R.string.intent_extra_pid);
    }

    public static final String INTENT_EXTRA_PROC_NAME(Context context) {
        return context.getResources().getString(R.string.intent_extra_proc_name);
    }

    public static final String INTENT_EXTRA_COMM_SERVICE_PID(Context context) {
        return context.getResources().getString(R.string.intent_extra_comm_service_pid);
    }

    public static final String INTENT_EXTRA_GTC_MESSAGE_PATH(Context context) {
        return context.getResources().getString(R.string.intent_extra_gtc_message_path);
    }

    public static final String INTENT_EXTRA_GTC_MESSAGE_PAYLOAD(Context context) {
        return context.getResources().getString(R.string.intent_extra_gtc_message_payload);
    }

    // --------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------

    public static final String NOTI_TITLE_RESOURCE_LOGGER(Context context) {
        return context.getResources().getString(R.string.noti_title_resource_logger);
    }

    public static final String NOTI_TEXT_RESOURCE_LOGGER(Context context) {
        return context.getResources().getString(R.string.noti_text_resource_logger);
    }

    public static final String NOTI_TITLE_FRAME_LOGGER(Context context) {
        return context.getResources().getString(R.string.noti_title_frame_logger);
    }

    public static final String NOTI_TEXT_FRAME_LOGGER(Context context) {
        return context.getResources().getString(R.string.noti_text_frame_logger);
    }

    public static final String NOTI_TITLE_COMMUNICATOR(Context context) {
        return context.getResources().getString(R.string.noti_title_communications);
    }

    public static final String NOTI_TEXT_COMMUNICATOR(Context context) {
        return context.getResources().getString(R.string.noti_text_communications);
    }

}