#!/usr/bin/env bash

FLOWDROID='soot-infoflow-cmd-jar-with-dependencies.jar'
ANDROID_PLATFORMS='/Users/slehr/Library/Android/sdk/platforms/'
SOURCES_AND_SINKS='sources_and_sinks_cv.txt'

# APK='apks/tf_obj.apk'
APK='apks/sp_admin.apk'

java -jar $FLOWDROID -a $APK -p $ANDROID_PLATFORMS -s $SOURCES_AND_SINKS