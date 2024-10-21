#!/bin/bash

curpath=$(dirname "$(realpath $0)")
project_path=$(readlink -e "$curpath/../../")

function web_be_cron() {
    cd "$project_path/web/be"
    ./cron.sh
}



web_be_cron
