#!/usr/bin/env bash

# APP_PACKAGE_NAME='com.kettler.argpsc3d'
# ((NUM_TRIALS=1))            # number of trials to execute
# ((WAIT_INT_SECS=2))         # interval (in seconds) to wait between each reading during a trial

APP_PACKAGE_NAME=$1
NUM_TRIALS=$2
WAIT_INT_SECS=$3

ANDROID_DATA_DIR='/storage/self/primary/Android/data'

# -----------------------------------------------------------------------------------

# WARNING - WHEN TRYING TO GET CURRENT DATE ON A MAC, HAVE TO MAKE SURE THAT THE GNU COREUTILS
# PACKAGE IS INSTALLED FIRST !!!

# REF:  http://www.skybert.net/mac-os-x/make-os-x-into-a-comfortable-unix-workstation/

# (SL) CoreUtils Installation Location:  /usr/local/opt/coreutils/libexec/gnubin/

# -----------------------------------------------------------------------------------

TRIAL_START_TIME=$(date +%s%3N)

# -----------------------------------------------------------------------------------

adb shell monkey -p ${APP_PACKAGE_NAME} 1
PID=$(adb shell ps | grep ${APP_PACKAGE_NAME} | awk '{print $2}')

echo ""                         # spacer line
echo "--------------------------------------------------------------------------------"
echo ""                         # spacer line

echo "App PID: ${PID}"
echo "Trial start time (ms): ${TRIAL_START_TIME}"
echo ""                         # spacer line