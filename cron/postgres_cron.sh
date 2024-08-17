#!/bin/bash

POSTGRES_PROC_NAME="postgresql\/.*\/bin\/postgres"
POSTGRESQL_SCRIPT="/etc/init.d/postgresql"

if [ ! -f "$POSTGRESQL_SCRIPT" ]; then
	echo "$POSTGRESQL_SCRIPT is not exist"
	exit 1
fi

proc=$(ps -aef | grep "${POSTGRES_PROC_NAME}" | grep -v "grep")

if [ -z "$proc" ]; then
	"$POSTGRESQL_SCRIPT" stop
	"$POSTGRESQL_SCRIPT" start
	echo "done"
fi

