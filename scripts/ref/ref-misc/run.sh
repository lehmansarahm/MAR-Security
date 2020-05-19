#!/usr/bin/env bash

((RUN_TIME_MIN=10))		# per application under test
((TEST_MODE=1))			# (1) image list, (2) blank screen, (3) sanity check
APP_LIST=mugtracker3D	# mugtracker2D, mugtracker3D

# IMG_DIR=~/Projects/MAR_Security/scripts/imgs
# IMG_FILE_LIST='sanityCheck3D'	# sanityCheck2D, sanityCheck3D

IMG_DIR=~/Projects/coco/train2017
IMG_FILE_LIST='imagelist_person'

OUTPUT_DIR='output'
mkdir -p ${OUTPUT_DIR}

TODAY="$(date +'%F')"
NOW="$(date +'%H%M%S')"
OUTPUT_FILE="${OUTPUT_DIR}/${IMG_FILE_LIST}.${TODAY}.${NOW}.csv"

sh ./test.sh ${IMG_DIR} ${IMG_FILE_LIST} ${RUN_TIME_MIN} ${TEST_MODE} appLists/${APP_LIST}.txt | tee ${OUTPUT_FILE}
echo "Full trial log:  $OUTPUT_FILE"

# echo "Rebooting device ...."
# adb reboot