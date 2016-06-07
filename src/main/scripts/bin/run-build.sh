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
source $GCUBE_RELEASE_TOOLKIT_HOME/lib/shflags


LOCK_FILE=${TMP_DIR}/run-build-lock


FLAGS_HELP="USAGE: $0 [flags]"
DEFINE_string 'project' '' "ETICS project" 'p'
DEFINE_string 'projectconfig' '' "ETICS project configuration" 'c'
DEFINE_string 'module' '' "module to build. If not provided project is taken" 'm'
DEFINE_string 'moduleconfig' '' "ETICS module configuration to build. If not provided project configuration is taken" 'g'
DEFINE_string 'buildtype' '' "one of: development|candidate|release" 'v'
DEFINE_boolean 'rundt' false "whehter run or not deployment tests at the end of the build" 't'
FLAGS "$@" || exit 1
eval set -- "${FLAGS_ARGV}"


PROJECT_NAME=${FLAGS_project}
PROJECT_CONFIG=${FLAGS_projectconfig}


if [ -z "$PROJECT_NAME" ]; then
	echo "[build-d4s] --project must be specified" 1>&2
	exit 1
fi

if [ -z "$PROJECT_CONFIG" ]; then
	echo "[build-d4s] --projectconfig must be specified" 1>&2
	exit 1
fi

MODULE_NAME=$PROJECT_NAME
if [ -n "${FLAGS_module}" ]; then
	MODULE_NAME=${FLAGS_module}
fi

MODULE_CONFIG=$PROJECT_CONFIG
if [ -n "${FLAGS_moduleconfig}" ]; then
	MODULE_CONFIG=${FLAGS_moduleconfig}
fi

RUN_DT=${FLAGS_rundt}
BUILD_TYPE=${FLAGS_buildtype}
if [ "$BUILD_TYPE" != "development" -a "$BUILD_TYPE" != "candidate" -a "$BUILD_TYPE" != "release" ]
then
	echo "[build-d4s] ERROR!! buildType parameter must be on of: development|candidate|release" 1>&2
	exit 1
fi

#
#
# 0. makes sure no other builds are running

if [ -e "$LOCK_FILE" ]
then
	echo "[build-d4s] ERROR!! Another instance of build-d4s script is already running. Exiting..." 1>&2
	exit 1
fi

touch $LOCK_FILE

# set a trap on script exit to be sure the lock file is deleted before exit
trap "rm -f $LOCK_FILE" EXIT


#
# LOG FILE CREATION
# build-d4s output is not printed on standard out stream. Instead it is appended
# in a log file copied in repository build dirat the end of script execution.
# This script will not produce output on console except for error messages.
# Since this, the -q option (automatically inherited in all d4s-sa3-toolkit scripts)
# has no effect on this script

BUILDER_STDOUT=${TMP_DIR}/build-d4s_current_build_stdout
BUILDER_STDERR=${TMP_DIR}/build-d4s_current_build_stderr
echo "" > $BUILDER_STDOUT >> $BUILDER_STDERR 2>&1



#
# 0. first of all, print some information about system configuration
${GCUBE_RELEASE_TOOLKIT_HOME}/lib/print-sys-info.sh >> $BUILDER_STDOUT



#
# 1. clean workspace
echo "[build-d4s] cleaning etics workspace..." >> $BUILDER_STDOUT
#{ rm -rf ${ETICS_WORKSPACE} 2>&1 1>&3 | tee -a $BUILDER_STDERR >&3; } 3>> $BUILDER_STDOUT
#{ mkdir ${ETICS_WORKSPACE} 2>&1 1>&3 | tee -a $BUILDER_STDERR >&3; } 3>> $BUILDER_STDOUT
# delete everything excepting the repository folder
{ find $ETICS_WORKSPACE -maxdepth 1 -not -name "`basename ${ETICS_WORKSPACE}`"  -not -name "repository" -exec rm -rf {} + 2>&1 1>&3 | tee -a $BUILDER_STDERR >&3; } 3>> $BUILDER_STDOUT


#
# create tasks in tasktrackers
if [ -n "$TT_TRACKER" ]
then
  export TASKNAME=$MODULE_CONFIG@`hostname -s`_on_`date +%s | tr ' ' '_'`
  curl -X POST -H "Content-Type:application/xml" --data "<task><name>$TASKNAME</name><sh>btrt.$MODULE_NAME</sh><displayName>$MODULE_CONFIG @`hostname -s` ($BUILD_TYPE build)</displayName></task>" $TT_TRACKER
  export TaskTracker_Parent_Task=$TT_TRACKER/$TASKNAME/eticsbuild.$MODULE_CONFIG
  curl -X POST -H "Content-Type:application/xml" --data "<task><name>eticsbuild.$MODULE_CONFIG</name><sh>btrt.eticsbuild.$MODULE_NAME</sh></task>" $TT_TRACKER/$TASKNAME/
  curl -X POST -H "Content-Type:application/xml" --data "<task><name>movebuild.$MODULE_CONFIG</name><sh>btrt.movebuild.$MODULE_NAME</sh></task>" $TT_TRACKER/$TASKNAME/
fi

#
# 2. execute etics build
echo "[build-d4s] executing etics build..." >> $BUILDER_STDOUT
{ ${GCUBE_RELEASE_TOOLKIT_HOME}/lib/etics-builder.sh $PROJECT_NAME $PROJECT_CONFIG $MODULE_NAME $MODULE_CONFIG $BUILD_TYPE 2>&1 1>&3 | tee -a $BUILDER_STDERR >&3; } 3>> $BUILDER_STDOUT


#
# 3. determine current build name

if [ ! -d ${LOCAL_REPOSITORY}/${MODULE_CONFIG} ]
then
  { mkdir -p ${LOCAL_REPOSITORY}/${MODULE_CONFIG} 2>&1 1>&3 | tee -a $BUILDER_STDERR >&3; } 3>> $BUILDER_STDOUT
  echo "[build-d4s] directory ${LOCAL_REPOSITORY}/${MODULE_CONFIG} created" >> $BUILDER_STDOUT
fi

last_pwd=`pwd`
cd ${LOCAL_REPOSITORY}/${MODULE_CONFIG}

last_build=`ls | egrep "^BUILD_[0-9]+(.tar.gz)?$" | sed 's/^BUILD_//' | sed 's/\.tar\.gz$//' | sort -n | tail -1`

if [ -z $last_build ]
then
	last_build=0
fi
BUILD_NAME="BUILD_$(($last_build + 1))"

echo "[build-d4s] determinated build number: $BUILD_NAME" >> $BUILDER_STDOUT
cd $last_pwd 2>&1

BUILD_HOME_DIR=${LOCAL_REPOSITORY}/${MODULE_CONFIG}/${BUILD_NAME}



[ -n "$TT_TRACKER" ] && curl -X PUT -H "Content-Type:application/xml" --data "<task><progress>0.5</progress></task>" $TT_TRACKER/$TASKNAME/movebuild.$MODULE_CONFIG

#
# 4. move build to repository
echo "[build-d4s] executing ${GCUBE_RELEASE_TOOLKIT_HOME}/lib/move-etics-build.sh $ETICS_WORKSPACE ${BUILD_HOME_DIR}" >> $BUILDER_STDOUT
{ /bin/bash ${GCUBE_RELEASE_TOOLKIT_HOME}/lib/move-build.sh $ETICS_WORKSPACE ${BUILD_HOME_DIR} 2>&1 1>&3 | tee -a $BUILDER_STDERR >&3; } 3>> $BUILDER_STDOUT

[ -n "$TT_TRACKER" ] && curl -X PUT -H "Content-Type:application/xml" --data "<task><progress>1.0</progress></task>" $TT_TRACKER/$TASKNAME/movebuild.$MODULE_CONFIG


#
# add (or modify if already exist) the platform tag to build-status.xml. Needed to BTRT for writing URLs 
if [ $ETICS_BUILD_PLATFORM ]
then
    echo "Patching build-status.xml replacing platform attribute to match the ETICS_BUILD_PLATFORM..."
    if ! grep -q "platform=\"[^\"]*\"" ${BUILD_HOME_DIR}/build-status.xml
    then
      sed -i "s/<project\ /<project\ platform=\"$ETICS_BUILD_PLATFORM\" /" ${BUILD_HOME_DIR}/build-status.xml
    fi 
    sed -i "s/platform=\"[^\"]*\"/platform=\"$ETICS_BUILD_PLATFORM\"/" ${BUILD_HOME_DIR}/build-status.xml
fi


#
# generate packages-report.xml
echo "[build-d4s] executing generate-packages-report... " >> $BUILDER_STDOUT
{ ${GCUBE_RELEASE_TOOLKIT_HOME}/bin/generate-packages-report.sh $MODULE_CONFIG $(($last_build + 1)) $(($last_build + 1)) 2>&1 1>&3 | tee -a $BUILDER_STDERR >&3; } 3>> $BUILDER_STDOUT


#
# generate configurations-report.xml
echo "[build-d4s] executing generating configurations-report.xml... " >> $BUILDER_STDOUT
java -cp "$JAVA_CP" org.gcube.tools.report.configurations.ConfigurationsReportGenerator --eticsws $ETICS_WS_ENDPOINT --output $BUILD_HOME_DIR/reports/configurations-report.xml --buildstatus $BUILD_HOME_DIR/reports/build-status.xml


#
# run distribution scripts
echo "[run-build] executing ${GCUBE_RELEASE_TOOLKIT_HOME}/bin/run-distribution.sh --configsreport $BUILD_HOME_DIR/reports/configurations-report.xml --buildtype $BUILD_TYPE"
{ ${GCUBE_RELEASE_TOOLKIT_HOME}/bin/run-distribution.sh --configsreport $BUILD_HOME_DIR/reports/configurations-report.xml --buildtype $BUILD_TYPE 2>&1 1>&3 | tee -a $BUILDER_STDERR >&3; } 3>> $BUILDER_STDOUT


echo "[run-build] executing python ${GCUBE_RELEASE_TOOLKIT_HOME}/bin/github-release.py $TMP_DIR/distribution/distribution.xml $BUILD_TYPE $GITHUB_RELEASE_FOLDER"
{ PYTHONPATH=/opt/etics/lib/python2.6/site-packages:/opt/etics/lib/python2.6/site-packages/:/opt/etics/ext/lib/python2.6/:/opt/etics/ext/lib/python2.6/site-packages/ python ${GCUBE_RELEASE_TOOLKIT_HOME}/bin/github-release.py $TMP_DIR/distribution/distribution.xml $BUILD_TYPE $GITHUB_RELEASE_FOLDER 2>&1 1>&3 | tee -a $BUILDER_STDERR >&3; } 3>> $BUILDER_STDOUT


cp -R $TMP_DIR/distribution $BUILD_HOME_DIR/reports/distribution
#for retrocompatibility with Distribution Site
cp $BUILD_HOME_DIR/reports/distribution/distribution.xml $BUILD_HOME_DIR/reports/distribution.xml


#
# 6. move BUILDER_OUTPUT into buildHome directory
echo "That's all folks!" >> $BUILDER_STDOUT
mv $BUILDER_STDOUT ${BUILD_HOME_DIR}/builder_output 2>&1
mv $BUILDER_STDERR ${BUILD_HOME_DIR}/builder_stderr 2>&1
echo "$BUILD_NAME for configuration $MODULE_CONFIG successfully carried out" >&6


#
# run deployment tests
if [ ${FLAGS_rundt} -eq ${FLAGS_TRUE} ]; then
 echo "[run-build] executing ${GCUBE_RELEASE_TOOLKIT_HOME}/bin/run-deployment-tests.sh $PROJECT_CONFIG $BUILD_HOME_DIR org.gcube.deploytest-new org.gcube.deploytest-new.RELEASE_NEW"
${GCUBE_RELEASE_TOOLKIT_HOME}/bin/run-deployment-tests.sh $PROJECT_CONFIG $BUILD_HOME_DIR org.gcube.deploytest-new org.gcube.deploytest-new.RELEASE_NEW
fi



# submit the build results to the etics-issue-tracker to publish the build issues in the ETICS Portal
curl -v http://etics.esl.eng.it/etics-issue-tracker/rest/issues/remoteBuild?url=http://eticsbuild2.research-infrastructures.eu/BuildReport/bdownload/AllBuilds/${MODULE_CONFIG}/${BUILD_NAME}
