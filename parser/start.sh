#!/bin/bash

JAR_DIR="build/libs/"
JAR_NAME="parser.jar"
LOG_DIR="/var/log/parser/"


curpath=$(dirname "$(realpath $0)")
cd "$curpath"

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

echo "start growacastle api parser program"
echo ""
java -jar "${JAR_DIR}/${JAR_NAME}"
exit 0
