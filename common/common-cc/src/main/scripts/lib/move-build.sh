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
# This script moves a build from position $1 to $2
# Usually it is used after a local build to move the build from  $ETICS_WORKSPACE
# to the local repository

if [ $# -ne 2 ]
then
  echo "[_move-build] ERROR!! Wrong paramets number. Exiting..."
  echo "[_move-build] Usage: `basename $0` currentLocation newLocation"
  exit 1
fi


CURRENT_LOCATION=$1
NEW_LOCATION=$2
TMP_LOCATION="$2_TMP"

echo "[_move_build] moving build from $CURRENT_LOCATION to $TMP_LOCATION"
mkdir -p $TMP_LOCATION

echo "[_move_build] build-status.xml"
cp -R $CURRENT_LOCATION/reports/build-status.xml $TMP_LOCATION/build-status.xml

echo "[_move_build] dist/ directory"
cp -R $CURRENT_LOCATION/dist $TMP_LOCATION/dist

echo "[_move_build] reports/ directory"
cp -R $CURRENT_LOCATION/reports $TMP_LOCATION/reports

echo "[_move_build] co-sizes.xml"
mkdir -p $TMP_LOCATION/reports/d4s-co-sizes
mv $CURRENT_LOCATION/co-sizes.xml $TMP_LOCATION/reports/d4s-co-sizes/co-sizes.xml

echo "[_move_build] renaming $TMP_LOCATION in $NEW_LOCATION..."
mv $TMP_LOCATION $NEW_LOCATION
echo "[_move_build] DONE"

