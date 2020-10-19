#!/usr/bin/env bash

OUTPUT_DIR='output'
mkdir -p ${OUTPUT_DIR}

TODAY="$(date +'%F')"
NOW="$(date +'%H%M%S')"
OUTPUT_FILE="${OUTPUT_DIR}/${TODAY}.${NOW}.csv"

sh ./test.sh | tee ${OUTPUT_FILE}
echo "Full trial log:  $OUTPUT_FILE"