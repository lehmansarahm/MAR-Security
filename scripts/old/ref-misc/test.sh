#!/usr/bin/env bash

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

IMG_DIR=$1
IMG_FILE_LIST=$2
RUN_TIME_MIN=$3
TEST_MODE=$4
APP_LIST=$5

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

((WAIT_INT_SECS=5))                                     # interval (in seconds) to wait between each reading during a trial
((NUM_TRIALS=${RUN_TIME_MIN} * 60 / ${WAIT_INT_SECS}))  # number of trials to execute

OUTPUT_DIR_PARENT='output'                              # name of output parent directory

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------
#
#   AR Compass Map 3D - com.kettler.argpsc3d
#   Google Translate -  com.google.android.apps.translate
#   IKEA Place -        com.inter_ikea.place
#
#   Augmented Faces -   com.google.ar.sceneform.samples.augmentedfaces
#   Hello SceneForm -   com.google.ar.sceneform.samples.hellosceneform
#
# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

# APP_PACKAGE_NAMES=('edu.temple.attack_prototypes.vuforia.v0' 'edu.temple.attack_prototypes.vuforia.v1' 'edu.temple.attack_prototypes.vuforia.v2')
# APP_NAMES=('mug_orig' 'mug_v1' 'mug_v2')

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

echo ""                                 # spacer line
echo "--------------------------------------------------------------------------------"
echo "    MAR SECURITY - AUTOMATED TEST SUITE"
echo "--------------------------------------------------------------------------------"
echo ""                                 # spacer line

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

print_app_header() {
    echo ""                             # spacer line
    echo "--------------------------------------------------------------------------------"
    echo "    MAR SECURITY - TESTING APPLICATION: ${APP_NAME}"
    echo "--------------------------------------------------------------------------------"
    echo ""                             # spacer line

    OUTPUT_DIR="${OUTPUT_DIR_PARENT}/${APP_NAME}"
    mkdir -p ${OUTPUT_DIR_PARENT}
    mkdir -p ${OUTPUT_DIR}

    TODAY="$(date +'%F')"
    NOW="$(date +'%H%M%S')"
    OUTPUT_FILE="${IMG_FILE_LIST}.${TODAY}.${NOW}.csv"

    echo "Application output directory created at: ${OUTPUT_DIR}"
    echo "Output will be written to file: $OUTPUT_FILE"
    echo ""                             # spacer line

    # -----------------------------------------------------------------------------------
    # -----------------------------------------------------------------------------------

    echo "Running test script with params: "
    echo "\t Application package name: \t\t ${PACKAGE}"
    echo "\t Application local name: \t\t ${APP_NAME}"
    echo "\t Run time (min): \t\t\t ${RUN_TIME_MIN}"
    echo "\t Data collection interval (seconds): \t ${WAIT_INT_SECS}"
    echo "\t Total number of trials: \t\t ${NUM_TRIALS}"
}

print_app_footer() {
    echo ""                         # spacer line
    echo "--------------------------------------------------------------------------------"
    echo "\t MAR SECURITY - APPLICATION TESTING TRIAL COMPLETE"
    echo "--------------------------------------------------------------------------------"
    echo "Output available: ${OUTPUT_DIR}/$OUTPUT_FILE"
    echo "Snapshots available: ${OUTPUT_DIR}/snapshots/"
    echo "--------------------------------------------------------------------------------"
    echo ""                         # spacer line
}

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

for APP_INFO in $(cat ${APP_LIST}); do

    PACKAGE=${APP_INFO%,*}      # first term of line, delimited by comma
    APP_NAME=${APP_INFO##*,}    # second term of line, delimited by comma

    print_app_header

    case ${TEST_MODE} in 

        1)  # test by image list
            echo "\t Reading from image directory: \t\t ${IMG_DIR}"
            echo "\t Reading from image file list: \t\t ${IMG_FILE_LIST}"
            echo ""                             # spacer line

            sh ./test_img_list.sh ${PACKAGE} ${NUM_TRIALS} ${WAIT_INT_SECS} ${IMG_DIR} imgLists/${IMG_FILE_LIST}.txt | tee ${OUTPUT_DIR}/${OUTPUT_FILE} ;;

        2)  # test with blank screen
            echo ""                             # spacer line
            sh ./test_blank_screen.sh ${PACKAGE} ${NUM_TRIALS} ${WAIT_INT_SECS} | tee ${OUTPUT_DIR}/${OUTPUT_FILE} ;;

        3)  # sanity checking test
            echo "\t Reading from image directory: \t\t ${IMG_DIR}"
            echo "\t Reading from image file list: \t\t ${IMG_FILE_LIST}"
            echo ""                             # spacer line

            sh ./test_sanity_check.sh ${PACKAGE} ${NUM_TRIALS} ${WAIT_INT_SECS} ${IMG_DIR} imgLists/${IMG_FILE_LIST}.txt | tee ${OUTPUT_DIR}/${OUTPUT_FILE} ;;

        *)  # default case
            echo ""                             # spacer line
            echo "SOMETHING WENT WRONG.  CAN'T CONTINUE WITH TEST SUITE ...  SHUTTING DOWN ..." ;;

    esac

    DATA_DIR="/storage/self/primary/Android/data"
    adb pull ${DATA_DIR}/${PACKAGE}/files ${OUTPUT_DIR}/snapshots/
    adb shell rm ${DATA_DIR}/${PACKAGE}/files/*

    print_app_footer 

done