#!/bin/bash

################################################################################
#                                                                              #
# functions declaration                                                        #
#                                                                              #
################################################################################


log_debug() {
        echo "[run-distribution][DEBUG] $1"
}

log_info() {
        echo "[run-distribution][INFO] $1"
}

log_error() {
        echo "[run-distribution][ERROR] $1"
}



log_fatal() {
        echo "[run-distribution][FATAL] $1"
}



################################################################################
#                                                                              #
# script entry point                                                           #
#                                                                              #
################################################################################



######################
# initialisation and commandline parsing
#
source $GCUBE_RELEASE_TOOLKIT_HOME/etc/bootstrap.sh
source $GCUBE_RELEASE_TOOLKIT_HOME/lib/shflags

FLAGS_HELP="USAGE: $0 [flags]"
DEFINE_string 'buildtype' '' "one of: development|candidate|release" 'b'
DEFINE_string 'configsreport' '' "location of configurations-report.xml" 'c'
#DEFINE_string 'eticsworkspace' '' "path to the etics workspace" 'w'
#DEFINE_string 'url' '' "registration client endpoint" 'u'
#DEFINE_string 'scope' '' "scope (e.g. /gcube/devsec)" 's'
DEFINE_boolean 'dryrun' false "no act. Perform just a simulation" 'n'
#DEFINE_boolean 'snapshots' false "accept snapshot versions" 'p'
#DEFINE_boolean 'releasenotes' true "whether create or not release notes" 'r'
FLAGS "$@" || exit 1
eval set -- "${FLAGS_ARGV}"



######################
# checks pre-requisites to run the script
#
[ -z "$GLOBUS_LOCATION" ] && log_fatal "GLOBUS_LOCATION is not set. Cannot continue, exiting..." && exit 1;

BUILD_TYPE=${FLAGS_buildtype}

if [ "$BUILD_TYPE" != "development" -a "$BUILD_TYPE" != "candidate" -a "$BUILD_TYPE" != "release" ]
then
        log_fatal "[run-distribution] ERROR!! buildType parameter must be one of: development|candidate|release"
        exit 1
fi

ACCEPT_SNAPSHOTS=""
GENERATE_RELEASENOTES="true"

if [ "$BUILD_TYPE" == "development" ]
then
    ACCEPT_SNAPSHOTS="--snapshots"
    GENERATE_RELEASENOTES="false"
fi

ETICS_WORKSPACE=$(readlink -fn $ETICS_WORKSPACE)
SG_URL=`eval eval "echo \$\{${BUILD_TYPE}_SG\}"`
SCOPE=`eval eval "echo \$\{${BUILD_TYPE}_SCOPE\}"`

source $GLOBUS_LOCATION/bin/gcore-load-env
DRGCLASSPATH=$GCUBE_RELEASE_TOOLKIT_HOME/lib/java/*

log_info "ETICS_WORKSPACE=$ETICS_WORKSPACE"
log_info "SG_URL=$SG_URL"
log_info "SCOPE=$SCOPE"
log_info "CLASSPATH=$CLASSPATH"
log_info "DRGCLASSPATH=$DRGCLASSPATH"

DIST_TMPDIR=$TMP_DIR/dist-tmp
mkdir $TMP_DIR 2> /dev/null

WORKINGDIR=$TMP_DIR/distribution
mkdir -p $SG_WORKINGDIR 2> /dev/null

#initialize distribution report
DISTRIBUTION_REPORT=$WORKINGDIR/distribution.xml
DISTRIBUTION_LOG_REPORT=$WORKINGDIR/distribution_log.xml
RELEASENOTES_FILE=$WORKINGDIR/releasenotes.xml
UNLOCKED_CONFIGS_FILE=${FLAGS_configsreport}


rm -rf $WORKINGDIR $DIST_TMPDIR 2> /dev/null
mkdir -p $DIST_TMPDIR
mkdir -p $WORKINGDIR

log_info "initializing distribution report..."
java -cp "$DRGCLASSPATH" org.gcube.tools.DistributionReportGenerator init --dreport $DISTRIBUTION_REPORT --logreport $DISTRIBUTION_LOG_REPORT $ACCEPT_SNAPSHOTS



if [ $GENERATE_RELEASENOTES == "true" ]; then
    log_info "initializing releasenotes report..."
    java -cp "$DRGCLASSPATH" org.gcube.tools.ReleaseNotesGenerator init --releasenotes $RELEASENOTES_FILE --logreport $DISTRIBUTION_LOG_REPORT
    #log_info "creating releasedconfigs report..."
    #java -cp "$DRGCLASSPATH" org.gcube.tools.ReleasedConfigurationsReportGenerator --output $UNLOCKED_CONFIGS_FILE --buildstatus $ETICS_WORKSPACE/reports/build-status.xml
fi

#select all artifacts
log_info "finding artifacts in workspace..."
ALL_ARTIFACTS=`find $ETICS_WORKSPACE -name "*servicearchive*" -and \( -path "*/tgz/*" -or -path "*/target/*" \)`
echo $ALL_ARTIFACTS | tr ' ' '\n' > $WORKINGDIR/distribution_artifacts_list.txt
log_info "artifacts stored in distribution_artifacts_list.txt"

#extract all profiles
for i in $ALL_ARTIFACTS; do
        TARGZ_FILENAME=`basename $i`
		echo "***** processing $TARGZ_FILENAME *****"

        #extract the package
        rm -rf $DIST_TMPDIR/* 2> /dev/null
        rm report.xml 2> /dev/null
        OLDDIR=`pwd`
        cd $DIST_TMPDIR
        tar xzf $i
        cd $OLDDIR

        PROFILE_FILE=$WORKINGDIR/profile_$TARGZ_FILENAME.xml
        cp $DIST_TMPDIR/profile.xml $PROFILE_FILE

        #call sgclient
        log_info "registering profile at SoftwareGateway..."
        java -cp "$CLASSPATH:$DRGCLASSPATH" org.gcube.vremanagement.softwaregateway.client.RegisterProfileClient $SG_URL $SCOPE $PROFILE_FILE
        EXIT_CODE=`echo $?`
        if [ $EXIT_CODE -ne 0 ]; then
                echo $PROFILE_FILE >> $WORKINGDIR/distribution_exceptions.txt
        fi

        REPORT_FILE=$WORKINGDIR/report_$TARGZ_FILENAME.xml
        REPORT_FILE_OPT=""
        if [ -e "report.xml" ]; then
                sed 's/&/&amp;/g' report.xml > $REPORT_FILE
                REPORT_FILE_OPT="--packages  $REPORT_FILE"
        fi


    	TMP=${i#$ETICS_WORKSPACE/}
    	ETICS_REF=${TMP%%/*}


        #call DistributionReportGenerator
        log_info "calling DistributionReportGenerator..."
        java -cp "$DRGCLASSPATH" org.gcube.tools.DistributionReportGenerator add --dreport $DISTRIBUTION_REPORT $REPORT_FILE_OPT --artifactdir $DIST_TMPDIR --artifactname $ETICS_REF --logreport $DISTRIBUTION_LOG_REPORT $ACCEPT_SNAPSHOTS


        #invoke releasenotes generator
        if [ $GENERATE_RELEASENOTES == "true" ]; then
        		log_info "callng ReleaseNotesGenerator..."
                java -cp "$DRGCLASSPATH" org.gcube.tools.ReleaseNotesGenerator add --releasenotes $RELEASENOTES_FILE  --sadir $DIST_TMPDIR --artifactname $ETICS_REF --releasedreport $UNLOCKED_CONFIGS_FILE  --logreport $DISTRIBUTION_LOG_REPORT
        fi
        
        echo -e "\n\n\n"
done
