#!/bin/bash

curpath=$(dirname "$(realpath $0)")

# cd "$curpath/../h2"
# ./cron.sh

# cd "$curpath/../parser"
cd "../parser"
./cron.sh
