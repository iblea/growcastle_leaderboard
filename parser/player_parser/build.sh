#!/bin/bash

curpath=$(dirname "$(realpath $0)")
cd "$curpath"

LOG_DIR="/var/log/parser/"

if [ ! -d "${LOG_DIR}" ]; then
    mkdir -p "${LOG_DIR}"
fi

JPA_SETTINGS_FILE="src/main/resources/META-INF/persistence.xml"

if [ ! -f "${JPA_SETTINGS_FILE}" ]; then
    echo "${JPA_SETTINGS_FILE} is not exist"
    echo "Please check the file"
    echo "========================================="
    echo "cd src/main/resources/META-INF"
    echo "cp -r persistence.sample.xml persistence.xml"
    exit 1
fi

LOG4J_SETTINGS_FILE="src/main/resources/log4j2.properties"
if [ ! -f "${LOG4J_SETTINGS_FILE}" ]; then
    echo "${LOG4J_SETTINGS_FILE} is not exist"
    echo "Please check the file"
    echo "========================================="
    echo "cd src/main/resources"
    echo "cp -r log4j2.properties.sample log4j2.properties"
    exit 1
fi

./gradlew build
# ./gradlew build --refresh-dependencies
