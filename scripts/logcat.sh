#!/usr/bin/env bash

if [[ $# -eq 0 ]] ; then APP_NAME="MugTracker"; else APP_NAME=$1; fi
adb logcat | grep ${APP_NAME}