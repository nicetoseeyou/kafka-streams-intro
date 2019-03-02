#!/bin/sh
baseDir=$(cd `dirname $0`; pwd)
parentDir=$(cd `dirname $baseDir`; pwd)
CLASS_PATH=${parentDir}/lib

leftInputTopic=$1
rightInputTopic=$2
rightStateStore=$3
configFile=$4

nohup java -Dlogback.configurationFile=${parentDir}/config/logback.xml -cp ${CLASS_PATH}/*:${CLASS_PATH}/${project.build.finalName}.jar fuck.hsbc.kafka.stream.filter.FilterApp ${leftInputTopic} ${rightInputTopic} ${rightStateStore} ${configFile} </dev/null >/dev/null 2>&1 &
