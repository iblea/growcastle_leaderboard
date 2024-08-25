#!/bin/bash

SH_CONFIG="script_config"
MODULE_INTEGRITY=1


curpath=$(dirname "$(realpath $0)")
cd "$curpath"
CONF_PATH="$curpath/conf"

if [ ! -f "$CONF_PATH/$SH_CONFIG" ]; then
    exit 1
fi

source $CONF_PATH/$SH_CONFIG

if [ -z "$VENV_NAME" ]; then
    echo "no VENV_NAME variable"
    exit 1
fi

if [ -z "$MAIN_SCRIPT" ]; then
    echo "no MAIN_SCRIPT variable"
    exit 1
fi

if [ -z "$FREEZE_PATH " ]; then
    echo "no FREEZE_PATH variable"
    exit 1
fi



if [ ! -d "$VENV_NAME" ]; then
    echo "VENV no settings... set first."
    python3 -m venv "$VENV_NAME"
fi

source "$VENV_NAME/bin/activate"

if [ ! -f "$CONF_PATH/$FREEZE_PATH" ]; then
    echo "cannot find pip3 modules information"
    # pip3 install requests
    # pip3 install discord
    exit 1
fi

if [ "$MODULE_INTEGRITY" != "0" ]; then
    modules=()
    if [ -f "$CONF_PATH/$FREEZE_PATH" ]; then
        freeze_file=$(cat "$CONF_PATH/$FREEZE_PATH" | awk -F '==' '{ print $1 }' )
        modules=($freeze_file)  # 리스트로 변환
        # echo "./$VENV_NAME/bin/python3 -m pip freeze > ./$FREEZE_PATH"
    fi

    current_module_list=$(python3 -m pip freeze 2>/dev/null)
    for module in "${modules[@]}"; do
        is_module=$(echo "${current_module_list}" | grep "${module}==")
        if [[ $is_module != "" ]]; then
            continue
        fi
        echo "$module is not found, install first"
        echo "install command"
        echo "./$VENV_NAME/bin/python3 -m pip install $module"
        echo " or "
        echo "source $VENV_NAME/bin/activate; pip3 install -r $CONF_PATH/$FREEZE_PATH; deactivate"
        deactivate
        exit 1
    done
fi

# $VENV_NAME/bin/python $MAIN_SCRIPT

deactivate

echo "require verify done"

