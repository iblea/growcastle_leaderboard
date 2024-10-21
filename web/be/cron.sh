#!/bin/bash

curpath=$(dirname "$(realpath $0)")
cd "$curpath"

CONF_PATH="./src/main/resources/build.cnf"

if [ ! -f "$CONF_PATH" ]; then
    echo "config file [${CONF_PATH}] is not exist"
    exit 1
fi
source "${CONF_PATH}"

if [ ! -f "${JAR_DIR}/${JAR_NAME}" ]; then
    echo "${JAR_DIR}/${JAR_NAME} is not exist"
	exit 1
fi

proc=$(ps -aef | grep "java -jar.*${JAR_NAME}" | grep -v "grep")
if [ -z "$proc" ]; then
    cd "$curpath"
    echo -n "" > output.log
    nohup ./start.sh >> output.log 2>&1 &
    echo "start web backend program"
else
    echo "already running"
fi
