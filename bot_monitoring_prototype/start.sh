#!/bin/bash

SH_CONFIG="script_config"


curpath=$(dirname $(realpath $0))
cd "$curpath"
CONF_PATH="$curpath/conf"

if [ ! -f "$CONF_PATH/$SH_CONFIG" ]; then
    exit 1
fi

source "$CONF_PATH/$SH_CONFIG"

if [ -z "$VENV_NAME" ]; then
    echo "no VENV_NAME variable"
    exit 1
fi

if [ -z "$MAIN_SCRIPT" ]; then
    echo "no MAIN_SCRIPT variable"
    exit 1
fi


# install
# python3 -m venv $VENV_NAME
# if not create 'activate' file
# python3 -m venv --without-pip $VENV_NAME
# curl https://bootstrap.pypa.io/get-pip.py | $VENV_NAME/bin/python3

source "$VENV_NAME/bin/activate"

# echo "pip list"
# pip freeze
# pip install -r pip_freeze.txt
# $VENV_NAME/bin/pip3 install discord

echo "venv activate"
cd "$curpath/src/"
"$curpath/$VENV_NAME/bin/python" "$MAIN_SCRIPT"
ret_code=$?

deactivate
echo "venv deactivate"

exit $ret_code
