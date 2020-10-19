#!/usr/bin/env bash

DATA_STORAGE_PREFIX='/storage/self/primary/Android/data/'

# -----------------------------------------------------------------------

pull_for_pkg_name() {
    PACKAGE_NAME=$1

    mkdir out
    mkdir out/${PACKAGE_NAME}

	adb pull ${DATA_STORAGE_PREFIX}/${PACKAGE_NAME}/files out/${PACKAGE_NAME}
	adb shell rm ${DATA_STORAGE_PREFIX}/${PACKAGE_NAME}/files/*

	adb shell ls ${DATA_STORAGE_PREFIX}/${PACKAGE_NAME}/files
}

# -----------------------------------------------------------------------

pull_for_pkg_name 'edu.temple.mar_security.ml_kit'