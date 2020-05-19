#!/usr/bin/env bash

if [[ $# -eq 0 ]] ; then APP_NAME="MAR_Security_ResLib"; else APP_NAME=$1; fi
adb logcat | grep ${APP_NAME}