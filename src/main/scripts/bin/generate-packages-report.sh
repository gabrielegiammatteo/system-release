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
# This script generate packages-report.xml file for one or more builds
#
# This script accept 3 arguments:
#   configuration-name
#   fromBuildId
#   toBuildId
#

source $GCUBE_RELEASE_TOOLKIT_HOME/etc/bootstrap.sh


#
# 1. Parse input parameters
#
if [ $# -ne 3 ]
then
  echo "Usage: `basename $0` configurationName fromBuildId toBuildId"
  exit 1
fi

CONF_NAME=$1
FROM_BUILD=$2
TO_BUILD=$3

CONF_HOME=${LOCAL_REPOSITORY}/${CONF_NAME}

if [ ! -d $CONF_HOME ];
then
  echo "[generate-packages-report] invalid configuration-homeDir $CONF_HOME. Exiting..."
  exit -1
fi

echo "[generate-packages-report] Generating packages-report.xml for builds from ${FROM_BUILD} to ${TO_BUILD} in ${CONF_NAME}..."

for i in `seq ${FROM_BUILD} ${TO_BUILD}`;
do
  echo -en "[generate-packages-report] BUILD_${i}..."
  BUILD_HOME=${CONF_HOME}/BUILD_${i}

  if [ ! -d $BUILD_HOME ];
  then
    echo -e "\t[NOT FOUND]"
    continue
  fi

  java -cp $JAVA_CP it.eng.d4s.sa3.cli.GeneratePackagesReport ${LOCAL_REPOSITORY} ${CONF_NAME} BUILD_${i} ${GCUBE_RELEASE_TOOLKIT_HOME}/etc/modulemappings.properties > /dev/null

  echo -e "\t[OK]";  #<--- latest line before "done" keyword must have a ;

done

cd $OLD_PWD
