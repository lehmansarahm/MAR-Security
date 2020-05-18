#!/usr/bin/env bash

((RUN_TIME_MIN=1))                                     # amount of time (in minutes) to run tests
((WAIT_INT_SECS=5))                                     # interval (in seconds) to wait between each reading during a trial
((NUM_TRIALS=60 / ${WAIT_INT_SECS} * ${RUN_TIME_MIN}))  # number of trials to execute

OUTPUT_DIR_PARENT='output'                              # name of output parent directory

# -----------------------------------------------------------------------------------
#   AR Compass Map 3D - com.kettler.argpsc3d
#   Google Translate -  com.google.android.apps.translate
#   IKEA Place -        com.inter_ikea.place
# -----------------------------------------------------------------------------------

APP_PACKAGE_NAMES=('com.kettler.argpsc3d' 'com.google.android.apps.translate')
APP_NAMES=('ar_compass' 'google_translate')

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

echo ""                                 # spacer line
echo "--------------------------------------------------------------------------------"
echo "    MAR SECURITY - AUTOMATED TEST SUITE"
echo "--------------------------------------------------------------------------------"
echo ""                                 # spacer line

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

for i in "${!APP_PACKAGE_NAMES[@]}"
do

    PACKAGE=${APP_PACKAGE_NAMES[i]}
    APP=${APP_NAMES[i]}

    echo ""                             # spacer line
    echo "--------------------------------------------------------------------------------"
    echo "    MAR SECURITY - TESTING APPLICATION: ${APP}"
    echo "--------------------------------------------------------------------------------"
    echo ""                             # spacer line

    OUTPUT_DIR="${OUTPUT_DIR_PARENT}/${APP}"
    mkdir -p ${OUTPUT_DIR_PARENT}
    mkdir -p ${OUTPUT_DIR}

    TODAY="$(date +'%F')"
    NOW="$(date +'%H%M%S')"
    OUTPUT_FILE="${TODAY}.${NOW}.csv"

    echo "Application output directory created at: ${OUTPUT_DIR}"
    echo "Output will be written to file: $OUTPUT_FILE"
    echo ""                             # spacer line

    # -----------------------------------------------------------------------------------
    # -----------------------------------------------------------------------------------

    echo "Running test script with params: "
    echo "\t Application package name: \t\t ${PACKAGE}"
    echo "\t Run time (min): \t\t\t ${RUN_TIME_MIN}"
    echo "\t Data collection interval (seconds): \t ${WAIT_INT_SECS}"
    echo "\t Total number of trials: \t\t ${NUM_TRIALS}"
    echo ""                             # spacer line

    sh ./test_indiv.sh ${PACKAGE} ${NUM_TRIALS} ${WAIT_INT_SECS} | tee ${OUTPUT_DIR}/${OUTPUT_FILE}

    # -----------------------------------------------------------------------------------
    # -----------------------------------------------------------------------------------

    echo ""                         # spacer line
    echo "--------------------------------------------------------------------------------"
    echo "\t MAR SECURITY - APPLICATION TESTING TRIAL COMPLETE"
    echo "--------------------------------------------------------------------------------"
    echo "Output available: ${OUTPUT_DIR}/$OUTPUT_FILE"
    echo "--------------------------------------------------------------------------------"
    echo ""                         # spacer line

done