#!/usr/bin/env bash

adb pull /storage/self/primary/Android/data/edu.temple.attack_prototypes.v1 snapshots/
adb shell rm /storage/self/primary/Android/data/edu.temple.attack_prototypes.v1/files/*

adb pull /storage/self/primary/Android/data/edu.temple.attack_prototypes.v2 snapshots/
adb shell rm /storage/self/primary/Android/data/edu.temple.attack_prototypes.v2/files/*

adb pull /storage/self/primary/Android/data/edu.temple.attack_prototypes.v3 snapshots/
adb shell rm /storage/self/primary/Android/data/edu.temple.attack_prototypes.v3/files/*

adb pull /storage/self/primary/Android/data/edu.temple.attack_prototypes.v4 snapshots/
adb shell rm /storage/self/primary/Android/data/edu.temple.attack_prototypes.v4/files/*

adb pull /storage/self/primary/Android/data/edu.temple.attack_prototypes.vuforia.v1 snapshots/
adb shell rm /storage/self/primary/Android/data/edu.temple.attack_prototypes.vuforia.v1/files/*

adb pull /storage/self/primary/Android/data/edu.temple.attack_prototypes.vuforia.v2 snapshots/
adb shell rm /storage/self/primary/Android/data/edu.temple.attack_prototypes.vuforia.v2/files/*

adb pull /storage/self/primary/Android/data/edu.temple.attack_prototypes.vuforia.v3 snapshots/
adb shell rm /storage/self/primary/Android/data/edu.temple.attack_prototypes.vuforia.v3/files/*

adb pull /storage/self/primary/Android/data/edu.temple.attack_prototypes.vuforia.v4 snapshots/
adb shell rm /storage/self/primary/Android/data/edu.temple.attack_prototypes.vuforia.v4/files/*