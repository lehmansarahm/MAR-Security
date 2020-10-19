#!/usr/bin/env bash

adb pull /storage/self/primary/Android/data/edu.temple.tf_for_poets.headless out/
adb shell rm /storage/self/primary/Android/data/edu.temple.tf_for_poets.headless/files/*