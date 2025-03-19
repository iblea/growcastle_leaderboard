#!/bin/bash

curpath=$(dirname "$(realpath $0)")
cd "$curpath"

parser_dirs=(
    "player_parser"
)

for parser_dir in "${parser_dirs[@]}"; do
    cd "$curpath/$parser_dir/"
    ./cron.sh
    cd - >/dev/null 2>&1
done
