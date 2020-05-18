#!/usr/bin/env bash

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

PACKAGE=$1
APP_NAME=$2

WAIT_TIME_PER_IMG=$3
READINGS_PER_IMG=$4
TOTAL_TRIAL_TIME=$5

IMG_FILE_LISTS=("$@") # IMG_FILE_LISTS=$6

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

OUTPUT_DIR_PARENT='output'
mkdir -p ${OUTPUT_DIR_PARENT}

OUTPUT_DIR="${OUTPUT_DIR_PARENT}/${APP_NAME}"
mkdir -p ${OUTPUT_DIR}

ANDROID_DATA_DIR='/storage/self/primary/Android/data'
APP_OUTPUT_DIR="${ANDROID_DATA_DIR}/${PACKAGE}"

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

SHOW_ALT=false
((CHROME_RESET_LIMIT=20))

SODA=$(cat imgLists_custom/soda_all.txt)
SODA=($SODA)

((SODA_LIMIT=${#SODA[@]}))
((SODA_INDEX=0))

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

print_header() {
    echo ""                                 # spacer line
    echo "--------------------------------------------------------------------------------"
    echo " \t MAR SECURITY - AUTOMATED TEST SUITE"
    echo "--------------------------------------------------------------------------------"
    echo ""                                 # spacer line

    echo "--------------------------------------------------------------------------------"
    echo " \t TESTING APPLICATION: ${APP_NAME}"
    echo "--------------------------------------------------------------------------------"
    echo ""                                 # spacer line

    echo "Application output directory created at: ${OUTPUT_DIR}"
    echo "Output will be written to file: $OUTPUT_FILE"
    echo ""                                 # spacer line

    echo "Running test script with params: "
    echo "\t Application package name: \t\t ${PACKAGE}"
    echo "\t Application local name: \t\t ${APP_NAME}"
    echo "\t Data collection interval (seconds): \t ${WAIT_TIME_PER_IMG}"
    echo "\t Readings per image: \t ${READINGS_PER_IMG}"
    echo "\t Run time (milliseconds): \t\t\t ${TOTAL_TRIAL_TIME}"
    echo "\t Reading from image file list: \t\t ${IMG_FILE_LIST}"
    echo ""                                 # spacer line

    # -----------------------------------------------------------------------------------
    # -----------------------------------------------------------------------------------

    # WARNING - WHEN TRYING TO GET CURRENT DATE ON A MAC, HAVE TO MAKE SURE THAT THE GNU COREUTILS
    # PACKAGE IS INSTALLED FIRST !!!

    # REF:  http://www.skybert.net/mac-os-x/make-os-x-into-a-comfortable-unix-workstation/

    # (SL) CoreUtils Installation Location:  /usr/local/opt/coreutils/libexec/gnubin/

    # -----------------------------------------------------------------------------------
    # -----------------------------------------------------------------------------------

    TRIAL_START_TIME=$(gdate +%s%3N)

    adb shell monkey -p ${PACKAGE} 1
    sleep ${WAIT_TIME_PER_IMG}

    PID=$(adb shell ps | grep ${PACKAGE} | awk '{print $2}')

    echo ""                         # spacer line
    echo "--------------------------------------------------------------------------------"
    echo ""                         # spacer line

    echo "App PID: ${PID}"
    echo "Trial start time (ms): ${TRIAL_START_TIME}"
    echo ""                         # spacer line

    HEADER="TRIAL,ELAPSED_TIME_MS,IMG_FILE,CPU_PERC,MEM_PERC,\
    EM_STOR_SIZE_KB,EM_STOR_AVAIL_KB,EM_STOR_USED_KB,EM_STOR_USE_PERC,APP_DIR_SIZE_KB,\
    TOTAL_MEM_KB,FREE_MEM_KB,AVAIL_MEM_KB,\
    FLAGS,UTIME,STIME,CUTIME,CSTIME,PRIORITY,NUM_THREADS,START_TIME,RSS"

    echo ${HEADER} | tr -d ''      # can't figure out how to prevent it from printing whitespace on line breaks...

    sleep ${WAIT_TIME_PER_IMG}
}

print_resource_line() {
    FILENAME=$1

    # -----------------------------------------------------------------------------------
    #                       "TOP" COLUMN LABELS
    # -----------------------------------------------------------------------------------
    # PID   USER    PR  NI  VIRT    RES     SHR     S   [%CPU]  %MEM    TIME+   ARGS
    # 26176 u0_a235 10  -10 3.8G    184M    153M    S   45.1    5.1     0:05.13 com.kettler.arg+
    # -----------------------------------------------------------------------------------

    TOP=$(adb shell top -m 20 -n 1 | grep ${PID})
    # echo ${TOP}

    CPU=$(echo ${TOP} | awk '{print $9}')
    MEM=$(echo ${TOP} | awk '{print $10}')

    # -----------------------------------------------------------------------------------
    #                       "PROC/MEMINFO" FIELDS
    # -----------------------------------------------------------------------------------
    # MemTotal: 3667960 kB              [ indices in sets of three ]
    # MemFree: 127516 kB
    # MemAvailable: 947532 kB
    # Buffers: 46768 kB
    # Cached: 900644 kB
    # SwapCached: 58328 kB
    # Active: 946020 kB
    # Inactive: 725956 kB
    # Active(anon): 448680 kB
    # Inactive(anon): 289788 kB
    # Active(file): 497340 kB
    # Inactive(file): 436168 kB
    # Unevictable: 12804 kB
    # Mlocked: 3084 kB
    # RbinTotal: 327680 kB
    # RbinAlloced: 327680 kB
    # RbinPool: 0 kB RbinFree: 0 kB
    # SwapTotal: 2097148 kB
    # SwapFree: 1635920 kB
    # Dirty: 4 kB Writeback: 0 kB
    # AnonPages: 731480 kB
    # Mapped: 522324 kB
    # Shmem: 1764 kB
    # Slab: 254792 kB
    # SReclaimable: 78588 kB
    # SUnreclaim: 176204 kB
    # KernelStack: 41200 kB
    # PageTables: 61936 kB
    # NFS_Unstable: 0 kB
    # Bounce: 0 kB
    # WritebackTmp: 0 kB
    # CommitLimit: 3931128 kB
    # Committed_AS: 90845228 kB
    # VmallocTotal: 263061440 kB
    # VmallocUsed: 0 kB
    # VmallocChunk: 0 kB
    # CmaTotal: 237568 kB
    # CmaFree: 7740 kB
    # -----------------------------------------------------------------------------------

    MEM_INFO=$(adb shell cat proc/meminfo)
    # echo ${MEM_INFO}

    TOTAL_MEM=$(echo ${MEM_INFO} | awk '{print $2}')
    FREE_MEM=$(echo ${MEM_INFO} | awk '{print $5}')
    AVAIL_MEM=$(echo ${MEM_INFO} | awk '{print $8}')

    # -----------------------------------------------------------------------------------
    #                      "STORAGE STATS" FIELDS (kB???)
    # -----------------------------------------------------------------------------------
    # Filesystem    1K-blocks    Used   Available   Use%    Mounted on
    # -----------------------------------------------------------------------------------

    STORAGE_STATS=$(adb shell df /storage/emulated)
    # echo ${STORAGE_STATS}

    STORAGE_SIZE=$(echo ${STORAGE_STATS} | awk '{print $9}')
    STORAGE_AVAIL=$(echo ${STORAGE_STATS} | awk '{print $11}')
    STORAGE_USED=$(echo ${STORAGE_STATS} | awk '{print $10}')
    STORAGE_USED_PERC=$(echo ${STORAGE_STATS} | awk '{print $12}')

    APP_OUTPUT_DIR_CHECK=$(adb shell [ -d ${APP_OUTPUT_DIR} ] && echo 'exists')
    if [[ ${APP_OUTPUT_DIR_CHECK} == 'exists' ]] ; then
        APP_DIR_STATS=$(adb shell du -s ${APP_OUTPUT_DIR})
        APP_DIR_SIZE=$(echo ${APP_DIR_STATS} | awk '{print $1}')
        # echo ${APP_DIR_SIZE}
    else
        APP_DIR_SIZE="DNE"
    fi

    # -----------------------------------------------------------------------------------
    #                       "PROC STAT" FIELDS
    # -----------------------------------------------------------------------------------
    # PID = 1, FLAGS = 9, UTIME = 14, STIME = 15, CUTIME = 16, CSTIME = 17, PRIORITY = 18,
    # NUM_THREADS = 20, START_TIME = 22, RSS = 24
    # -----------------------------------------------------------------------------------

    PROC_STATS=$(adb shell cat proc/${PID}/stat)
    # echo ${PROC_STATS}

    PID_CHECK=$(echo ${PROC_STATS} | awk '{print $1}')
    # echo ${PID_CHECK}

    FLAGS=$(echo ${PROC_STATS} | awk '{print $9}')
    UTIME=$(echo ${PROC_STATS} | awk '{print $14}')
    STIME=$(echo ${PROC_STATS} | awk '{print $15}')
    CUTIME=$(echo ${PROC_STATS} | awk '{print $16}')
    CSTIME=$(echo ${PROC_STATS} | awk '{print $17}')
    PRIORITY=$(echo ${PROC_STATS} | awk '{print $18}')
    NUM_THREADS=$(echo ${PROC_STATS} | awk '{print $20}')
    START_TIME=$(echo ${PROC_STATS} | awk '{print $22}')
    RSS=$(echo ${PROC_STATS} | awk '{print $24}')

    # -----------------------------------------------------------------------------------

    TRIAL_CURRENT_TIME=$(gdate +%s%3N)
    ELAPSED_TRIAL_TIME=$((TRIAL_CURRENT_TIME - $TRIAL_START_TIME))

    LINE="${TRIAL_COUNTER},${ELAPSED_TRIAL_TIME},${FILENAME},${CPU},${MEM},\
    ${STORAGE_SIZE},${STORAGE_AVAIL},${STORAGE_USED},${STORAGE_USED_PERC},${APP_DIR_SIZE},\
    ${TOTAL_MEM},${FREE_MEM},${AVAIL_MEM},\
    ${FLAGS},${UTIME},${STIME},${CUTIME},${CSTIME},${PRIORITY},${NUM_THREADS},${START_TIME},${RSS}"
    echo ${LINE} | tr -d ' ' | tr -d '%'        # remove any lingering whitespace, special chars
}

kill_and_print_footer() {
    adb shell am force-stop ${PACKAGE}
    adb shell am kill ${PACKAGE}
    pkill -a -i "Google Chrome"

    adb pull ${APP_OUTPUT_DIR}/files ${OUTPUT_DIR}/snapshots/
    adb shell rm ${APP_OUTPUT_DIR}/files/*

    # -----------------------------------------------------------------------------------
    # -----------------------------------------------------------------------------------

    echo ""                             # spacer line
    echo "--------------------------------------------------------------------------------"
    echo " \t APPLICATION TESTING TRIAL COMPLETE"
    echo "--------------------------------------------------------------------------------"
    echo "Output available: $OUTPUT_FILE"
    echo "Snapshots available: ${OUTPUT_DIR}/snapshots/"
    echo "--------------------------------------------------------------------------------"
    echo ""                             # spacer line
}

run_trial_for_img_list() {
    LIST_FILE=$1
    print_header

    for IMG_FILE in $(cat ${LIST_FILE}); do

        open -a 'Google Chrome.app' ${IMG_FILE}
        sleep ${WAIT_TIME_PER_IMG}

        for (( i=1; i<=${READINGS_PER_IMG}; i++)); do

            print_resource_line ${IMG_FILE}
            TRIAL_COUNTER=$((TRIAL_COUNTER+1))
            sleep 1

        done

        TRIAL_COUNT_RESET=$((TRIAL_COUNTER%CHROME_RESET_LIMIT))
        if [ $TRIAL_COUNT_RESET = 0 ] ; then 

            if [ $SHOW_ALT = true ] ; then run_alt_segment; fi
            pkill -a -i "Google Chrome"
            sleep ${WAIT_TIME_PER_IMG}

        fi

        if [ $ELAPSED_TRIAL_TIME -gt $TOTAL_TRIAL_TIME ]; then break; fi

    done 

    kill_and_print_footer
    sleep ${WAIT_TIME_PER_IMG}
    sleep ${WAIT_TIME_PER_IMG}
}

run_alt_segment() {
    ORIG_TRIAL_COUNTER=${TRIAL_COUNTER}

    FILENAME="$(echo ${SODA[$SODA_INDEX]})"
    SODA_INDEX=$((SODA_INDEX+1))
    if [ $SODA_INDEX = $SODA_LIMIT ]; then
        ((SODA_INDEX=0))
    fi

    for (( i=1; i<=${READINGS_PER_IMG}; i++)); do
        TRIAL_COUNTER=${ORIG_TRIAL_COUNTER}.${i}
        open -a 'Google Chrome.app' ${FILENAME}
        sleep ${WAIT_TIME_PER_IMG}
        print_resource_line ${FILENAME}
    done

    TRIAL_COUNTER=${ORIG_TRIAL_COUNTER}
}

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

for IMG_FILE_LIST in ${IMG_FILE_LISTS[*]}; do

    # filter out the other parameters that get lumped in with the rest of the array...
    if [[ $IMG_FILE_LIST == $PACKAGE* ]]; then continue; fi
    if [[ $IMG_FILE_LIST == $APP_NAME* ]]; then continue; fi
    if [[ $IMG_FILE_LIST == $WAIT_TIME_PER_IMG* ]]; then continue; fi
    if [[ $IMG_FILE_LIST == $READINGS_PER_IMG* ]]; then continue; fi
    if [[ $IMG_FILE_LIST == $TOTAL_TRIAL_TIME* ]]; then continue; fi

    TODAY="$(date +'%F')"
    NOW="$(date +'%H%M%S')"
    OUTPUT_FILE="${OUTPUT_DIR}/${IMG_FILE_LIST}.${TODAY}.${NOW}.csv"

    ((TRIAL_COUNTER=1))
    run_trial_for_img_list ${IMG_FILE_LIST} >> ${OUTPUT_FILE}
    
done

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------