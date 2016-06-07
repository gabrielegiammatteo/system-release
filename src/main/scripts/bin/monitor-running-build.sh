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

source $GCUBE_RELEASE_TOOLKIT_HOME/etc/bootstrap.sh

counter=1

while [ 1 ]
do
  OLDPWD=`pwd`
  cd $ETICS_WORKSPACE

  CHECKEDOUT_MODULES=`ls -d * | wc -l 2> /dev/null`
  COMPILED_MODULES=`ls -d dist/org.gcube/* | wc -l 2> /dev/null`
  FAILED_MODULES=` grep "Failed" reports/build-status.xml  | wc -l 2> /dev/null`
  clear
  echo -en "\033[0;0H"
  echo "                                         gCube  RUNNING BUILD MONITOR"
  echo "`date`                                                                             i: $counter"
  echo -e "\n\n"
  echo -e "Modules Statistics:\n-----------------------"
  echo -e "# checked-out modules:\t\033[0;34m$CHECKEDOUT_MODULES\033[0;30m"
  echo -e "# compiled modules:\t\033[0;32m$COMPILED_MODULES ($FAILED_MODULES failed)\033[0;30m"

  cd $OLDPWD

  sleep 2m
  counter=$((counter+1))
done
