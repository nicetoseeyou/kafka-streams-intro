#!/bin/sh
baseDir=$(cd `dirname $0`; pwd)
parentDir=$(cd `dirname $baseDir`; pwd)
CLASS_PATH=${parentDir}/lib

existingPid=`ps -ef | grep "fuck.hsbc.kafka.stream.filter.FilterApp" | grep -v grep | awk '{print $2}'`

if [ ${existingPid} ]; then
    echo "Will terminate FilterApp with PID=${existingPid}"
    kill ${existingPid}
else
    echo "No PID for FilterApp Found."
fi