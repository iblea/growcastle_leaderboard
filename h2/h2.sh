#!/bin/bash

curpath=$(dirname "$(realpath $0)")
cd "$curpath"

if [ ! -f "$curpath/config_h2" ]; then
    echo "no file config_h2"
    echo "=================================================="
    echo "cd $curpath"
    echo "cp -r config_h2.sample config_h2"
    echo "=================================================="
    exit 1
fi

source "$curpath/config_h2"
h2_jar_full_path="$H2_JAR_PATH/$H2_JAR_NAME"

if [ ! -f "$h2_jar_full_path" ]; then
	echo "$h2_jar_full_path not found"
	echo "modify 'H2_JAR_PATH', 'H2_JAR_NAME' variable"
	exit 1
fi

java -cp "$h2_jar_full_path:$H2DRIVERS:$CLASSPATH" org.h2.tools.Console \
    -webAllowOthers -webPort $webPort \
    -tcpAllowOthers -tcpPort $tcpPort \
    -pgAllowOthers -pgPort $pgPort \
    "$@"

