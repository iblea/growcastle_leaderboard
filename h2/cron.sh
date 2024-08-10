#!/bin/bash


proc=$(ps -aef | grep "java.*h2.*\.jar.*Console" | grep -v "grep")
if [ -z "$proc" ]; then
    curpath=$(dirname "$(realpath $0)")
    cd "$curpath"
    echo -n "" > output.log
    nohup ./h2.sh >> output.log 2>&1 &
    echo "started"
else
    echo "already running"
fi
