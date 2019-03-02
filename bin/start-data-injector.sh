#!/bin/sh
baseDir=$(cd `dirname $0`; pwd)
parentDir=$(cd `dirname $baseDir`; pwd)
CLASS_PATH=${parentDir}/lib

targetTopic=$1
keyTemplateFile=$2
valueTemplateFile=$3
keyIncluded=$4
keyExcluded=$5
batchSize=$6
configFile=$7
totalBatches=$8

nohup java -Dlogback.configurationFile=${parentDir}/config/injector-log.xml -cp ${CLASS_PATH}/*:${CLASS_PATH}/${project.build.finalName}.jar fuck.hsbc.kafka.stream.inject.InjectApp ${targetTopic} ${keyTemplateFile} ${valueTemplateFile} ${keyIncluded} ${keyExcluded} ${batchSize} ${configFile} ${totalBatches} </dev/null >/dev/null 2>&1 &
