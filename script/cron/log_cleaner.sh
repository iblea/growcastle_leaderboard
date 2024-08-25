#!/bin/bash

LOG4J_DIR=/var/log/parser

if [ ! -d "${LOG4J_DIR}" ]; then
    exit 0
fi

find "$LOG4J_DIR" -type f -mtime +7 -exec rm -f {} \;