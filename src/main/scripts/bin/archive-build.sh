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
# This script one or more builds for a given configuration.
# Archiving operation consists in:
#   - remove artefacts ([BUILD_HOME]/dist directory)
#   - create a tar.gz from [BUILD_HOME]
#   - remove [BUILD_HOME] directory
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
  echo "[archive-build] invalid configuration-homeDir $CONF_HOME. Exiting..."
  exit -1
fi

echo "[archive-build] Archiving builds from ${FROM_BUILD} to ${TO_BUILD} in ${CONF_NAME}..."
echo -e "[archive-build]             del.TGZs\tcompressing\tdel.Dir"

OLD_PWD=`pwd`
cd $CONF_HOME

for i in `seq ${FROM_BUILD} ${TO_BUILD}`;
do
  echo -en "[archive-build] BUILD_${i}     "
  BUILD_HOME=BUILD_${i}
  
  if [ ! -d $BUILD_HOME ];
  then
    echo -e "\t\t\t\t\t[NOT FOUND]"
    continue
  fi

  

  rm -rf ${BUILD_HOME}/dist
  echo -en "[OK]"

  

  tar czf BUILD_${i}.tar.gz $BUILD_HOME
  echo -en  "\t    [OK]"

  

  rm -rf ${BUILD_HOME}
  echo -e "\t   [OK]";  #<--- latest line before "done" keyword must have a ;

done

cd $OLD_PWD
