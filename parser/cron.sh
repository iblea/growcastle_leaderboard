#!/bin/bash

curpath=$(dirname "$(realpath $0)")
cd "$curpath"

JAR_DIR="build/libs"
JAR_NAME="parser.jar"

if [ ! -f "${JAR_DIR}/${JAR_NAME}" ]; then
    echo "${JAR_DIR}/${JAR_NAME} is not exist"
fi

proc=$(ps -aef | grep "java -jar.*${JAR_NAME}" | grep -v "grep")
if [ -z "$proc" ]; then
    cd "$curpath"
    echo -n "" > output.log
    nohup ./start.sh >> output.log 2>&1 &
    echo "start growacastle api parser program"
else
    echo "already running"
fi
