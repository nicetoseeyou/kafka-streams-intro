#!/bin/sh
baseDir=$(cd `dirname $0`; pwd)
parentDir=$(cd `dirname $baseDir`; pwd)
CLASS_PATH=${parentDir}/lib

nohup java -Dlogback.configurationFile=${parentDir}/config/logback.xml -cp ${CLASS_PATH}/*:${CLASS_PATH}/${project.build.finalName}.jar fuck.hsbc.kafka.stream.filter.FilterApp </dev/null >/dev/null 2>&1 &
