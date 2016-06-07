#!/bin/bash
#
# d4s-sa3-toolkit
#
# Authors:
#     Gabriele Giammatteo [gabriele.giammatteo@eng.it]
#
# 2012
# ------------------------------------------------------------------------------
#
# $1 configuration directory
# $2 minimum number of builds to keep

source $GCUBE_RELEASE_TOOLKIT_HOME/etc/bootstrap.sh

if [ $# -ne 2 ]
then
  echo "Wrong number of arguments. Exiting..."
  exit 1
fi



ALLDIRS=`find $1/* -maxdepth 0 -type d`
if [ $? -ne 0 ];
then
 echo "Impossible to find subdirectories in $1"
 exit 1
fi

NUM=`ls -d1 $ALLDIRS | wc -l`

if [ "$NUM" -le	 "$2" ];
then
  echo "Less than $2 builds available. Not removing anything"
  exit 0
fi


TOBEDELETED=`ls -dt $ALLDIRS | tail -n1 | grep -v '^\.'`

echo "Directory selected for removal: $TOBEDELETED"
rm -rf $TOBEDELETED/*
rmdir $TOBEDELETED
echo "Directory removed. Exiting"
