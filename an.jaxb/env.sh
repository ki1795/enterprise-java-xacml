#!/bin/sh

if [ "${JAVA_HOME}" == "" ] ; then

echo "ERROR: JAVA_HOME not found in your environment."
echo "Please, set the JAVA_HOME variable in your environment to match the"
echo "location of the Java Virtual Machine you want to use."

else

AN_HOME=/nfs/users/roywang/an_current
export AN_HOME

ANT_HOME=${AN_HOME}/tools/apache-ant-1.7.0
export ANT_HOME

PATH=${PATH}:${ANT_HOME}/bin
export PATH

DEBUG=true
export DEBUG

OPTIMIZE=true
export OPTIMIZE

echo "Environments have been set."

fi

