#!/bin/bash
#
# d4s-sa3-toolkit
#
# Authors:
#     Gabriele Giammatteo [gabriele.giammatteo@eng.it]
#
# 2009
# ------------------------------------------------------------------------------
#
#
# To specify which builds schedule do not change this script. etc/scheduled-builds.conf
# should be changed instead.
#
#

source $GCUBE_RELEASE_TOOLKIT_HOME/etc/bootstrap.sh



BUILD_COMMAND="$GCUBE_RELEASE_TOOLKIT_HOME/bin/run-build.sh"

CONF_FILE="${GCUBE_RELEASE_TOOLKIT_HOME}/etc/scheduled-builds.conf"


for i in `egrep -v "^\ *(#.*)?$" $CONF_FILE | tr -s " " "+"` #white-spaces are repleaced with plus character here...
do
	PARAMS=`echo $i | tr -s "+" " "` #..and replaced back here

 	echo "[run-scheduled-builds] executing $BUILD_COMMAND $PARAMS"
	$BUILD_COMMAND $PARAMS
done

echo "[run-scheduled-builds] all scheduled builds have been executed. Exiting..."
