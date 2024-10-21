#!/bin/bash

JAR_DIR="build/libs"
JAR_NAME="growcastle-0.0.1-SNAPSHOT.jar"
LOG_DIR="/var/log/gc_backend/"


CONF_PATH="./src/main/resources/build.cnf"

curpath=$(dirname "$(realpath $0)")
cd "$curpath"

if [ ! -f "$CONF_PATH" ]; then
    echo "config file [${CONF_PATH}] is not exist"
    exit 1
fi
source "${CONF_PATH}"

cat settings.gradle.kts | grep "rootProject.name"

echo "log file dir path : ${LOG_DIR}"

if [ ! -d "${LOG_DIR}" ]; then
    echo "log file dir is not exist"
    mkdir -p "${LOG_DIR}"
fi

ls -al "${LOG_DIR}"
echo ""
echo ""
echo ""

if [ ! -f "${JAR_DIR}/${JAR_NAME}" ]; then
    echo "${JAR_DIR}/${JAR_NAME} is not exist"
    echo "Please build the project first"
    echo ""
    echo "cd ${curpath}"
    echo "gradlew build"
    echo "or"
    echo "gradlew build --refresh-dependencies"
    exit 1
fi

# h2 database check
# db_proc=$(ps -aef | grep "java.*h2.*\.jar.*Console" | grep -v "grep")
# if [ -z "$db_proc" ]; then
#     echo "database is not running"
#     exit 1
# fi


echo "start web backend program"
echo ""
java -jar "${JAR_DIR}/${JAR_NAME}"
exit 0
