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
    exit 1
fi

./gradlew build
# ./gradlew build --refresh-dependencies
