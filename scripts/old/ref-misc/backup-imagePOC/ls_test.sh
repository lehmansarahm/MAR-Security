#!/usr/bin/env bash

((NUM_TRIALS=7)) 
((TRIAL_COUNTER=1))

IMG_DIR=~/Projects/coco/test2017
FILE_LIST=$(ls ${IMG_DIR})

for FILE in $FILE_LIST; do

	echo ${IMG_DIR}/${FILE}
	open -a 'Google Chrome.app' ${IMG_DIR}/${FILE}
	sleep 5

	if [ $TRIAL_COUNTER = $NUM_TRIALS ] ; then break; else TRIAL_COUNTER=$((TRIAL_COUNTER+1)); fi

done

pkill -a -i "Google Chrome"