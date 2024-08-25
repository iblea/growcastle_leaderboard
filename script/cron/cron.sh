#!/bin/bash

curpath=$(dirname "$(realpath $0)")
project_path=$(readlink -e "$curpath/../../")


function db_cron() {
    # h2 database
    # cd "$curpath/../h2"
    # ./cron.sh

    # postgresql cron
    cd "$curpath"
    ./postgres_cron.sh
}

function parser_cron() {
    cd "$project_path/parser"
    ./cron.sh
}

function bot_cron() {
    cd "$project_path/discord_bot/prototype/"
    ./cron.sh
    cd "$project_path/discord_bot/bot_monitoring_prototype/"
    ./cron.sh
}



db_cron
parser_cron
bot_cron
