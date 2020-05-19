DATA_STORAGE_PREFIX='/storage/self/primary/Android/data/'
PACKAGE_NAME='edu.temple.mar_security.headless_tf'

PACKAGE_DATA_DIR="${DATA_STORAGE_PREFIX}${PACKAGE_NAME}/"
PACKAGE_FILES_DIR="${PACKAGE_DATA_DIR}files/"
PACKAGE_MOVIES_DIR="${PACKAGE_FILES_DIR}Movies/"

# -----------------------------------------------------------------------

adb shell mkdir ${PACKAGE_DATA_DIR}
adb shell mkdir ${PACKAGE_MOVIES_DIR}

adb push ../ref/long_video.mp4 ${PACKAGE_MOVIES_DIR}/long_video.mp4