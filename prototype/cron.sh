#!/bin/bash

SH_CONFIG="script_config"


curpath=$(dirname $(realpath $0))
CONF_PATH="$curpath/conf"

if [ -z "$curpath" ]; then
    echo "a"
    exit 1
fi

if [ ! -f "$CONF_PATH/$SH_CONFIG" ]; then
    echo "b"
    exit 1
fi

BOT_PATH="$curpath/start.sh"
source "$CONF_PATH/$SH_CONFIG"

if [ -z "$MAIN_SCRIPT" ]; then
    echo "no main script"
    exit 1
fi

proc=$(ps -aef | grep "$MAIN_SCRIPT" | grep -v "grep")
if [ "$proc" = "" ]; then
    if [ -x $BOT_PATH ]; then
        cd "$curpath"
        nohup $BOT_PATH &
        # $BOT_PATH
    fi
fi
