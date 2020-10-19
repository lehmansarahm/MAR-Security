#!/usr/bin/env bash

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

# PACKAGE='edu.temple.attack_prototypes.arcore.v0'
# APP_NAME='arcore_honest'

# PACKAGE='edu.temple.attack_prototypes.arcore.v2'
# APP_NAME='arcore_dishonest'

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

# PACKAGE='edu.temple.attack_prototypes.vuforia.v0_2d'
# APP_NAME='vuforia_honest'

# PACKAGE='edu.temple.attack_prototypes.vuforia.v0_2d'
# APP_NAME='vuforia_dishonest'

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

# PACKAGE='edu.temple.mar_security.prototypes_tflite'
# APP_NAME='tensorflow_honest'

PACKAGE='edu.temple.mar_security.prototypes_tflite_v2'
APP_NAME='tensorflow_dishonest'

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

((WAIT_TIME_PER_IMG=2))                     # Give app 2sec to recognize image
((READINGS_PER_IMG=1))

((TRIAL_TIME=5))							# 15 minutes per trial
((TOTAL_TRIAL_TIME=$TRIAL_TIME*60*1000))    # Convert minutes to milliseconds

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

# HONEST VS. DISHONEST APP COMPARISON
# IMG_FILE_LISTS=('imgLists_val/cat.txt')	# random pictures of cats interspersed with soda logos

# INPUT TYPE COMPARISON FOR DISHONEST ARCORE / VUFORIA
# - use "cat" list, but different "alt" sequence in "run_test_suite"
# - USE_ALT = true, soda_single				# positive - both recognize
# - USE_ALT = true, soda_all_but_one 		# negative - only dishonest recognizes
# - USE_ALT = false							# neutral - neither recognizes


# INPUT TYPE COMPARISON FOR DISHONEST TENSORFLOW
# IMG_FILE_LISTS=('imgLists_val/laptop.txt')			# positive - both recognize
# IMG_FILE_LISTS=('imgLists_val/dog.txt')				# negative - only dishonest recognizes
IMG_FILE_LISTS=('imgLists_val/traffic_light.txt')	# neutral - neither recognizes


# IMG_FILE_LISTS=('imgLists_val/cat.txt' 'imgLists_val/dog.txt' 'imgLists_val/laptop.txt' 
#	'imgLists_val/person.txt' 'imgLists_val/potted_plant.txt')

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

sh ./run_test_suite.sh ${PACKAGE} ${APP_NAME} \
	${WAIT_TIME_PER_IMG} ${READINGS_PER_IMG} ${TOTAL_TRIAL_TIME} \
	"${IMG_FILE_LISTS[@]}"

echo "Rebooting device ...."
adb reboot