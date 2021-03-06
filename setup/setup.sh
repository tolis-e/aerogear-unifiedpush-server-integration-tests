#!/bin/bash

# FIXME this should be replaced altogether by Arquillian Spacelift
# This script does following stuff:
# 1. Downloads UPS server
# 2. Downloads JBoss AS7 container
# 3. Patches JBoss AS7 container, to contain https setup

BASE_DIR=`pwd`
SCRIPT_DIR=$(dirname $(readlink -f $0))
DOWNLOAD_URL="http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.zip"
JBOSS_ZIP="$BASE_DIR/jboss-as-7.1.1.Final.zip"
JBOSS_HOME="$BASE_DIR/jboss-as-7.1.1.Final"
AG_UPS_REPO="$BASE_DIR/aerogear-unifiedpush-server"

function cloneRepository() {

    echo "[INFO] Cloning Aerogear UPS repository master"

    # FIXME this might not be necessary
    if [ -d "$AG_UPS_REPO" ]; then
        rm -rf $AG_UPS_REPO
    fi

    # We are cloning 0.10.0
    if [ ! -d "$AG_UPS_REPO" ]; then
        git clone -q https://github.com/aerogear/aerogear-unifiedpush-server.git $AG_UPS_REPO
        cd $AG_UPS_REPO
        #git branch --track 0.10.x origin/0.10.x
        #git checkout 0.10.x
        git checkout 35d15c91b7b4f01902e2fdb6a61d60506439a67c
        cd ..
    fi
}

function getAS7() {
    echo "[INFO] Downloading JBoss AS7 distribution"

    if [ ! -f "$JBOSS_ZIP" ]; then
        echo $DOWNLOAD_URL
        wget -c $DOWNLOAD_URL -P $BASE_DIR/
    fi

    # FIXME this might not be necessary
    if [ -d "$JBOSS_HOME" ]; then
        rm -rf $JBOSS_HOME
    fi

    echo "[INFO] Extracting JBoss AS7 distribution"

    if [ -f "$JBOSS_ZIP" ]; then
        unzip -qo $JBOSS_ZIP -d $BASE_DIR/
    else
        echo "[ERROR] The path does not contain a JBoss distribution" 1>&2
        exit 3
    fi
}


# Patches an instance of AS7/EAP6 via script
# It starts and stops the instance by itself
#
# $1 path to JBoss AS7/EAP6
# $2 is either domain or standalone
# $2 path to jboss-cli script
# $3 timeout
function patchContainer() {

    echo "[DEBUG] Starting JBoss container at $1"

    # runs JBoss container
    $1/bin/$2.sh > /dev/null & 

    echo "[DEBUG] Waiting for JBoss container to start"

    isRunning=0

    for i in {1..15}; do
        $1/bin/jboss-cli.sh --commands=connect > /dev/null

        if [[ $? == "0" ]]; then
            isRunning=1
             break
        fi

        echo "[DEBUG] Start of JBoss container is not yet finished, checking again in $4 second(s)"
        sleep $4
    done

    # another sleeping just to be sure
    sleep $4

    if [[ "${isRunning}" == "0" ]]; then
        echo "[ERROR] Patching process not finished due to JBoss container not being available" 1>&2
        exit 1
    fi

    echo "[DEBUG] JBoss container is now started"

    # wait for EAP6 to start
    echo "[INFO] Patching JBoss configuration using jboss_cli.sh script: $3"

    $1/bin/jboss-cli.sh --file=$3

    echo "[INFO] Patching process has been finished"

    # stops EAP6
    if [ "$2" == "domain" ]; then
        $1/bin/jboss-cli.sh --connect command=/host=master:shutdown > /dev/null
    else
        $1/bin/jboss-cli.sh --connect command=:shutdown > /dev/null
    fi

    # wait for EAP6 to stop
    isRunning=1
    for i in {1..15}; do
        $1/bin/jboss-cli.sh --commands=connect > /dev/null

        if [[ $? != "0" ]]; then
           isRunning=0
           break
        fi

        echo "[DEBUG] Shutdown of JBoss container is not yet finished, checking again in $4 second(s)"
        sleep $4
    done

    if [[ "${isRunning}" != "0" ]]; then
        echo "[ERROR] JBoss container was unable to shutdown for some reason" 1>&2
        exit 1
    fi

    echo "[DEBUG] JBoss container is now stopped"
}

cloneRepository
getAS7

#patchContainer $JBOSS_HOME $AG_UPS_REPO/databases/h2-database-config.cli 7

# copy keystore and truststore to the right directories
cp $SCRIPT_DIR/aerogear.keystore $JBOSS_HOME/standalone/configuration
cp $SCRIPT_DIR/aerogear.keystore $JBOSS_HOME/domain/configuration

cp $SCRIPT_DIR/aerogear.truststore $JBOSS_HOME/standalone/configuration
cp $SCRIPT_DIR/aerogear.truststore $JBOSS_HOME/domain/configuration

patchContainer $JBOSS_HOME standalone $SCRIPT_DIR/enable_https.cli 7
patchContainer $JBOSS_HOME domain $SCRIPT_DIR/enable_https_domain.cli 7
