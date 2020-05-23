DATA_STORAGE_PREFIX='/storage/self/primary/Android/data/'
VIDEO_NAME='fetch.mp4'

# -----------------------------------------------------------------------

init_for_pkg_name() {
    PACKAGE_NAME=$1
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

init_for_pkg_name 'edu.temple.edge_playground'