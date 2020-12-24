#!/usr/bin/env bash

PACKAGE='edu.temple.mar_security.mlkit_orth'                # app package name
APP_NAME='mal_orth'                                         # human-friendly app name

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

ANDROID_DATA_DIR='./storage/self/primary/Android/data'      # parent-level external directory
APP_DATA_DIR="${ANDROID_DATA_DIR}/${PACKAGE}"               # app-level external directory

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

OUTPUT_DIR_PARENT='output'
mkdir -p ${OUTPUT_DIR_PARENT}                               # create the output dir if DNE

OUTPUT_DIR="${OUTPUT_DIR_PARENT}/${APP_NAME}"
mkdir -p ${OUTPUT_DIR}                                      # create app-specific dir if DNE

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

# pull any residual artifacts from the app's external files directory
adb pull "${APP_DATA_DIR}/files" ${OUTPUT_DIR}
adb shell rm "${APP_DATA_DIR}/files/*"
adb shell ls "${APP_DATA_DIR}/files"