#!/usr/bin/env bash
SODA=$(cat imgLists_custom/soda_all.txt)
SODA=($SODA)

((SODA_LIMIT=${#SODA[@]}))
echo $SODA_LIMIT

((SODA_INDEX=0))
FILENAME="$(echo ${SODA[$SODA_INDEX]})"
echo $FILENAME

SODA_INDEX=$((SODA_INDEX+1))
FILENAME="$(echo ${SODA[$SODA_INDEX]})"
echo $FILENAME

((SODA_INDEX=5))
if [ $SODA_INDEX = $SODA_LIMIT ]; then
    ((SODA_INDEX=0))
fi

FILENAME="$(echo ${SODA[$SODA_INDEX]})"
echo $FILENAME