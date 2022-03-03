#!/usr/bin/env bash

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

# PACKAGE='edu.temple.mar_security.mlkit'                     # app package name
# APP_NAME='honest'                                           # human-friendly app name

PACKAGE='edu.temple.mar_security.mlkit_comp'                # app package name
APP_NAME='mal_comp'                                         # human-friendly app name

# PACKAGE='edu.temple.mar_security.mlkit_orth'                # app package name
# APP_NAME='mal_orth'                                         # human-friendly app name

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

adb shell monkey -p ${PACKAGE} 1
sleep ${WAIT_TIME_PER_READ}

PID=$(adb shell ps | grep ${PACKAGE} | awk '{print $2}')
adb shell top -m 20 | grep ${PID}
