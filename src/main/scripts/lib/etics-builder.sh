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
# This script executes a build using etics command line interface. In order to
# execute the build, the scripts needs to know the module to build, its configuration,
# in which project it is located, and the project's configuration (to inherit project level
# properties)
# Required information are specified by following command line parameters:
# $1 --> project-name
# $2 --> project-configuration
# $3 --> module-name
# $4 --> module-configuration
# $5 --> build-type: development|candidate|release
#

if [ $# -ne 5 ]
then
  echo "[_etics-builder] ERROR!! Wrong paramets number. Exiting..."
  echo "[_etics-builder] Usage: `basename $0` projectName projectConfiguration moduleName moduleConfiguration"
  echo "[_etics-builder] example: `basename $0` org.gcube org.gcube.1-6-0 org.gcube.application.aquamaps.aquamapsportlet org.gcube.application.aquamaps.aquamapsportlet.1-1-0"
  exit 1
fi

PROJECT_NAME=$1
PROJECT_CONFIG=$2
MODULE_NAME=$3
MODULE_CONFIG=$4
BUILD_TYPE=$5


SOURCE_STATISTICS=${GCUBE_RELEASE_TOOLKIT_HOME}/lib/co-sizes/sourceStatistics.jar


if [ -n "$ETICS_BUILD_PLATFORM" ]; then
	PLATFORM="--platform $ETICS_BUILD_PLATFORM"
else
	PLATFORM=""
fi

echo "[_etics-builder] change directory to $ETICS_WORKSPACE..."
OLD_PWD=`pwd`
cd $ETICS_WORKSPACE

if [ -n "$ETICS_CUSTOM_CONF" ];
then
	echo "[_etics-builder] Copying custom etics configuration from $ETICS_CUSTOM_CONF"
	cp $ETICS_CUSTOM_CONF $ETICS_WORKSPACE/etics.conf
fi

echo "[_etics-builder] executing etics-workspace-setup..."
${ETICS_HOME}/bin/etics-workspace-setup


[ -n "$TT_TRACKER" ] && curl -X POST -H "Content-Type:application/xml" --data "<task><name>etics.get-project</name><sh>etics.get-project.$MODULE_NAME</sh></task>" $TaskTracker_Parent_Task
[ -n "$TT_TRACKER" ] && curl -X PUT -H "Content-Type:application/xml" --data "<task><progress>0.5</progress></task>" $TaskTracker_Parent_Task/etics.get-project

echo "[_etics-builder] executing ${ETICS_HOME}/bin/etics-get-project $PROJECT_NAME..."
${ETICS_HOME}/bin/etics-get-project $PROJECT_NAME

[ -n "$TT_TRACKER" ] && curl -X PUT -H "Content-Type:application/xml" --data "<task><progress>1.0</progress></task>" $TaskTracker_Parent_Task/etics.get-project


if [ -d "$ETICS_GCUBE_ISSUE_CRAWLERS" ]
then
  ETICS_ISSUE_CRAWLERS_PROP="-pissues.crawlers-path=$ETICS_GCUBE_ISSUE_CRAWLERS"
else
  ETICS_ISSUE_CRAWLERS_PROP=""
fi


echo "[_etics-builder] executing ${ETICS_HOME}/bin/etics-checkout $PLATFORM --ignorelocking --shallowbindeps --continueonerror --verbose -c $MODULE_CONFIG --project-config $PROJECT_CONFIG $ETICS_ISSUE_CRAWLERS_PROP $ETICS_CHECKOUT_OPTS $MODULE_NAME"
echo "[_etics-builder] START CHECKOUT TIMESTAMP `date +%s`"
${ETICS_HOME}/bin/etics-checkout $PLATFORM --ignorelocking --shallowbindeps --continueonerror --verbose -c $MODULE_CONFIG --project-config $PROJECT_CONFIG $ETICS_ISSUE_CRAWLERS_PROP $ETICS_CHECKOUT_OPTS $MODULE_NAME
if [ "$?" -ne 0 ];
then
  echo "[_etics-builder] WARNING etics-checkout exits with non-zero value"
fi
echo "[_etics-builder] STOP CHECKOUT TIMESTAMP `date +%s`"


echo "[_etics-builder] generating co-sizes.xml Report..."
echo "[_etics-builder] executing java -cp $SOURCE_STATISTICS org.diligentproject.support.Workspace $ETICS_WORKSPACE $ETICS_WORKSPACE/co-sizes.xml"
java -cp $SOURCE_STATISTICS org.diligentproject.support.Workspace $ETICS_WORKSPACE $ETICS_WORKSPACE/co-sizes.xml
if [ "$?" -ne 0 ];
then
  echo "[_etics-builder] WARNING co-sizes.xml generator exits with non-zero value"
fi

#add stylesheet and move the co-sizes report to the location where it is visible from BTRT
sed -i 's/^<workspace/<?xml-stylesheet type="text\/xsl" href="sizeReport.xslt" ?><workspace/' $ETICS_WORKSPACE/co-sizes.xml
mkdir $ETICS_WORKSPACE/reports/d4s-co-sizes
cp $ETICS_WORKSPACE/co-sizes.xml $ETICS_WORKSPACE/reports/d4s-co-sizes
cp $GCUBE_RELEASE_TOOLKIT_HOME/lib/co-sizes/sizeReport.xslt $ETICS_WORKSPACE/reports/d4s-co-sizes

#patch for findbugs which fails if stage dir is not present
mkdir -p $ETICS_WORKSPACE/stage

if [ "$BUILD_TYPE" == "release" ];
then
  RELEASE_OPT="-e MAVEN_BUILD_TYPE=release -e MAVEN_SETTINGS=$GCUBE_RELEASE_TOOLKIT_HOME/etc/gcube-release-settings.xml -e PREPARE_GITHUB_RELEASE=true"
fi
if [ "$BUILD_TYPE" == "candidate" ];
then
  RELEASE_OPT="-e MAVEN_BUILD_TYPE=candidate -e MAVEN_SETTINGS=$GCUBE_RELEASE_TOOLKIT_HOME/etc/gcube-default-settings.xml"
fi

if [ "$BUILD_TYPE" == "development" ];
then
  RELEASE_OPT="-e MAVEN_BUILD_TYPE=development -e MAVEN_SETTINGS=$GCUBE_RELEASE_TOOLKIT_HOME/etc/gcube-default-settings.xml"
fi


echo "[_etics-builder] executing ${ETICS_HOME}/bin/etics-build --force --packagetypes "tgz:tar.gz" $PLATFORM --continueonerror --verbose $RELEASE_OPT $ETICS_BUILD_OPTS $MODULE_NAME"
echo "[_etics-builder] START BUILD TIMESTAMP `date +%s`"
${ETICS_HOME}/bin/etics-build --force --packagetypes "tgz:tar.gz" $PLATFORM --continueonerror --verbose $RELEASE_OPT $ETICS_BUILD_OPTS $MODULE_NAME

if [ "$?" -ne 0 ];
then
  echo "[_etics-builder] WARNING etics-build exits with non-zero value"
fi
echo "[_etics-executor] STOP BUILD TIMESTAMP `date +%s`"


cd $OLD_PWD
