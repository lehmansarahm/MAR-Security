#!/usr/bin/env bash

DATA_STORAGE_PREFIX='/storage/self/primary/Android/data/'


# APP_PKG_NAME='edu.temple.edge_playground'
# VIDEO_LIST=('text_25pt.mp4' 'text_35pt.mp4' 'text_45pt.mp4' 'text_55pt.mp4' 'text_65pt_crf0.mp4' 'text_65pt_crf23.mp4' 'text_65pt_crf37.mp4' 'text_65pt_crf51.mp4')
# VIDEO_LIST=('dogs_1_crf0.mp4' 'dogs_1_crf23.mp4' 'dogs_1_crf37.mp4' 'dogs_1_crf51.mp4' 'dogs_2.mp4' 'dogs_3.mp4' 'dogs_4.mp4' 'dogs_5.mp4')


# APP_PKG_NAME='edu.temple.mar_security.headless_fb'		# barcodes
APP_PKG_NAME='edu.temple.mar_security.headless_fb_mal'		# barcodes + text

VIDEO_LIST=('long_video.mp4')


# -----------------------------------------------------------------------

init_for_pkg_name() {
    PACKAGE_NAME=$1
	VIDEO_NAME=$2

	PACKAGE_DATA_DIR="${DATA_STORAGE_PREFIX}${PACKAGE_NAME}/"
	PACKAGE_FILES_DIR="${PACKAGE_DATA_DIR}files/"
	PACKAGE_MOVIES_DIR="${PACKAGE_FILES_DIR}Movies/"

	adb shell mkdir ${PACKAGE_DATA_DIR}
	adb shell mkdir ${PACKAGE_MOVIES_DIR}

	adb push ../ref/${VIDEO_NAME} ${PACKAGE_MOVIES_DIR}${VIDEO_NAME}
	adb shell ls ${PACKAGE_MOVIES_DIR}
}

# -----------------------------------------------------------------------

# init_for_pkg_name 'edu.temple.mar_security.headless_tf'

# init_for_pkg_name 'edu.temple.mar_security.headless_tf_mal'

# init_for_pkg_name 'edu.temple.mar_security.headless_fb'

# init_for_pkg_name 'edu.temple.mar_security.headless_fb_mal'

# -----------------------------------------------------------------------

for VIDEO in ${VIDEO_LIST[*]}; do

	init_for_pkg_name ${APP_PKG_NAME} ${VIDEO}
    
done