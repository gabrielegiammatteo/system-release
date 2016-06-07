#!/bin/bash
#
# gcube-release-toolkit
#
# Authors:
#     Gabriele Giammatteo [gabriele.giammatteo@eng.it]
#
# 2012
# ------------------------------------------------------------------------------
#
#

source $GCUBE_RELEASE_TOOLKIT_HOME/etc/bootstrap.sh

BUILD_TYPE=$1


if [ "$BUILD_TYPE" != "development" -a "$BUILD_TYPE" != "candidate" -a "$BUILD_TYPE" != "release" ]
then
	echo "[build-d4s] ERROR!! buildType parameter must be on of: development|candidate|release" 1>&2
	exit 1
fi

if [ -z "$BDIST_HOME" ]
then
	echo "BDIST_HOME not set. Exiting..."
	exit 1
fi

export SG_URL=`eval eval "echo \$\{${BUILD_TYPE}_SG\}"`
export SCOPE=`eval eval "echo \$\{${BUILD_TYPE}_SCOPE\}"`

if [ "$BUILD_TYPE" == "development" ]
then
	SNAPSHOT_OPT="--snapshots"
    RELEASENOTES_OPT="--noreleasenotes"
fi

echo "Using build-distribution at: $BDIST_HOME"

export GLOBUS_LOCATION=$BDIST_HOME/gCore
source $GLOBUS_LOCATION/bin/gcore-load-env

OLDDIR=`pwd`
cd $TMP_DIR

echo "Executing $BDIST_HOME/bin/distribution_reports_generator --eticsworkspace $ETICS_WORKSPACE --url $SG_URL --scope $SCOPE $SNAPSHOT_OPT $RELEASENOTES_OPT"
$BDIST_HOME/bin/distribution_reports_generator --eticsworkspace $ETICS_WORKSPACE --url $SG_URL --scope $SCOPE $SNAPSHOT_OPT $RELEASENOTES_OPT
 
cd $OLDDIR
