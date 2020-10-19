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

HEADER="TRIAL,ELAPSED_TIME_MS,CPU_PERC,MEM_PERC,\
EM_STOR_SIZE_KB,EM_STOR_AVAIL_KB,EM_STOR_USED_KB,EM_STOR_USE_PERC,APP_DIR_SIZE_KB,\
TOTAL_MEM_KB,FREE_MEM_KB,AVAIL_MEM_KB,\
FLAGS,UTIME,STIME,CUTIME,CSTIME,PRIORITY,NUM_THREADS,START_TIME,RSS"

echo ${HEADER} | tr -d ''      # can't figure out how to prevent it from printing whitespace on line breaks...

sleep ${WAIT_INT_SECS}

for (( i=1; i<=$NUM_TRIALS; i++ ))
do

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

    APP_DIR_STATS=$(adb shell du -s ${ANDROID_DATA_DIR}/${APP_PACKAGE_NAME})
    APP_DIR_SIZE=$(echo ${APP_DIR_STATS} | awk '{print $1}')
    # echo ${APP_DIR_SIZE}

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

    TRIAL_CURRENT_TIME=$(date +%s%3N)
    ELAPSED_TRIAL_TIME=$((TRIAL_CURRENT_TIME - $TRIAL_START_TIME))

    LINE="${i},${ELAPSED_TRIAL_TIME},${CPU},${MEM},\
    ${STORAGE_SIZE},${STORAGE_AVAIL},${STORAGE_USED},${STORAGE_USED_PERC},${APP_DIR_SIZE},\
    ${TOTAL_MEM},${FREE_MEM},${AVAIL_MEM},\
    ${FLAGS},${UTIME},${STIME},${CUTIME},${CSTIME},${PRIORITY},${NUM_THREADS},${START_TIME},${RSS}"
    echo ${LINE} | tr -d ' ' | tr -d '%'        # remove any lingering whitespace, special chars

    TRIAL_START_TIME=${TRIAL_CURRENT_TIME}
    sleep ${WAIT_INT_SECS}

done

# -----------------------------------------------------------------------------------

adb shell am force-stop ${APP_PACKAGE_NAME}
adb shell am kill ${APP_PACKAGE_NAME}