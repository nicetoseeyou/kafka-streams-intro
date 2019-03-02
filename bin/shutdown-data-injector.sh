#!/bin/sh
baseDir=$(cd `dirname $0`; pwd)
parentDir=$(cd `dirname $baseDir`; pwd)
CLASS_PATH=${parentDir}/lib

existingPid=`ps -ef | grep "fuck.hsbc.kafka.stream.inject.InjectApp" | grep -v grep | awk '{print $2}'`

if [ ${existingPid} ]; then
    echo "Will terminate InjectApp with PID=${existingPid}"
    kill ${existingPid}
else
    echo "No PID for InjectApp Found."
fi