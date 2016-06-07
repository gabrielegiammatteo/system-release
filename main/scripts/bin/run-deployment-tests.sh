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
#

source $GCUBE_RELEASE_TOOLKIT_HOME/etc/bootstrap.sh


LOCK_FILE=${TMP_DIR}/dt-d4s_lock

#
# parse command line parameters. This script gets 3 parameters

if [ $# -eq 4 ]
then
	PROJECT_CONFIG=$1
	BUILD_HOME=$2
	TESTSUITE=$3
	TESTSUITE_CONFIG=$4
else
  echo "[run-deployment-tests] ERROR!! Wrong paramets number. Exiting..." 1>&2
  echo "[run-deployment-tests] Usage: `basename $0` projectConfiguration buildName testsuite testsuiteConfiguration" 1>&2
  echo "[run-deployment-tests] example: `basename $0` org.gcube.2-8-0 BUILD_12 org.gcube.deploytest-new org.gcube.deploytest-new.TMP" 1>&2
  exit 1
fi

#
#
# 0. get assured no other tests or a build are running

if [ -e "$LOCK_FILE" ]
then
	echo "[build-d4s] ERROR!! Another instance of run-deployment-tests script is already running. Exiting..." 1>&2
	exit 1
fi


touch $LOCK_FILE

# set a trap on script exit to be sure the lock file is deleted before exit
trap "rm -f $LOCK_FILE" EXIT


STDOUT=${TMP_DIR}/dt_stdout
STDERR=${TMP_DIR}/dt_stderr
echo "" > $STDOUT >> $STDERR 2>&1


#
# 1. clean workspace
echo "[run-deployment-tests] cleaning workspace ${DT_WORKSPACE} ..." >> $STDOUT
{ rm -rf ${DT_WORKSPACE} 2>&1 1>&3 | tee -a $STDERR >&3; } 3>> $STDOUT
OLD_PWD=`pwd`
mkdir $DT_WORKSPACE
cd $DT_WORKSPACE
{ ${ETICS_HOME}/bin/etics-workspace-setup 2>&1 1>&3 | tee -a $STDERR >&3; } 3>> $STDOUT

if [ -n "$ETICS_CUSTOM_CONF" ];
then
	echo "[run-deployment-tests] Copying custom etics configuration from $ETICS_CUSTOM_CONF"
	cp $ETICS_CUSTOM_CONF $DT_WORKSPACE/etics.conf
fi

if [ -n "$ETICS_BUILD_PLATFORM" ]; then
	PLATFORM="--platform $ETICS_BUILD_PLATFORM"
else
	PLATFORM=""
fi


echo "[run-deployment-tests] executing etics-get-project org.gcube" >> $STDOUT
{ ${ETICS_HOME}/bin/etics-get-project org.gcube 2>&1 1>&3 | tee -a $STDERR >&3; } 3>> $STDOUT

echo "[run-deployment-tests] executing ${ETICS_HOME}/bin/etics-checkout --force --noask --ignorelocking --shallowbindeps --volatile=$PROJECT_CONFIG $PLATFORM --project-config $PROJECT_CONFIG --config $TESTSUITE_CONFIG $TESTSUITE" >> $STDOUT
{ ${ETICS_HOME}/bin/etics-checkout --force --noask --ignorelocking --shallowbindeps --volatile=$PROJECT_CONFIG $PLATFORM --project-config $PROJECT_CONFIG --config $TESTSUITE_CONFIG $TESTSUITE 2>&1 1>&3 | tee -a $STDERR >&3; } 3>> $STDOUT


echo "[run-deployment-tests] executing ${ETICS_HOME}/bin/etics-test --continueonerror -e DT_PORT=$DT_PORT -e DT_SCOPE=$DT_SCOPE -e DT_DELAY=$DT_DELAY -e DT_RM_HOST=$DT_RM_HOST -e DT_RM_PORT=$DT_RM_PORT -p nologtimestamp=True $TESTSUITE">> $STDOUT
{ ${ETICS_HOME}/bin/etics-test --continueonerror -e DT_PORT=$DT_PORT -e DT_SCOPE=$DT_SCOPE -e DT_DELAY=$DT_DELAY -e DT_RM_HOST=$DT_RM_HOST -e DT_RM_PORT=$DT_RM_PORT -p nologtimestamp=True $TESTSUITE 2>&1 1>&3 | tee -a $STDERR >&3; } 3>> $STDOUT



BUILD_DT_HOME_DIR=$BUILD_HOME/reports/dt
echo "[run-deployment-tests] storing reports in $BUILD_DT_HOME_DIR">> $STDOUT
mkdir -p $BUILD_DT_HOME_DIR

{ cp -vR reports/css reports/images reports/reportModuleDetail-$TESTSUITE*HEAD.html reports/reportModules.html $BUILD_DT_HOME_DIR 2>&1 1>&3 | tee -a $STDERR >&3; } 3>> $STDOUT

echo "[run-deployment-tests] creating dt.xml" >> $STDOUT
echo "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>" > ${TMP_DIR}/dt.xml
echo "<SoftwareArchives>" >> ${TMP_DIR}/dt.xml
for i in reports/reportModuleDetail-$TESTSUITE*HEAD.html
do
        tmp=`basename $i .html`
	aux3=${tmp#*-}
        aux4=`cat ${aux3%-org*}/deployment_component`
	aux5=`cat ${aux3%-org*}/deployment_status`
	echo "   <SoftwareArchive>" >> ${TMP_DIR}/dt.xml
        echo "      <Component>$aux4</Component>" >> ${TMP_DIR}/dt.xml
        echo "      <Report>"`echo $i|sed 's/^reports\//reports\/dt\//'`"</Report>" >> ${TMP_DIR}/dt.xml
        echo "      <Result>$aux5</Result>" >> ${TMP_DIR}/dt.xml
        echo "   </SoftwareArchive>" >> ${TMP_DIR}/dt.xml
done
echo "</SoftwareArchives>" >> ${TMP_DIR}/dt.xml
{ cat ${TMP_DIR}/dt.xml 2>&1 1>&3 | tee -a $STDERR >&3; } 3>> $STDOUT

{ cp ${TMP_DIR}/dt.xml ${BUILD_HOME}/dt.xml 2>&1 1>&3 | tee -a $STDERR >&3; } 3>> $STDOUT

{ cp $STDOUT $STDERR  ${BUILD_HOME}/ 2>&1 1>&3 | tee -a $STDERR >&3; } 3>> $STDOUT
