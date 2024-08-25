#!/bin/bash


process_info=$(ps -aef | grep "java.*h2" | grep -v "grep")

while [ -n "$process_info" ]; do
	echo "h2 process is already running."
	pkill -f "java.*h2"
	sleep 0.5
	echo "dying h2 process"
	process_info=$(ps -aef | grep "java.*h2" | grep -v "grep")
done


ps -aef | head -n1
ps -aef | grep "java.*h2" | grep -v "grep"

if [[ "$1" = "kill" ]]; then
	echo "kill h2"
	exit 0
fi

curpath=$(dirname "$(realpath $0)")
cd "$curpath"

if [ -f nohuup.out ]; then
	rm -f "./nohup.out"
fi

nohup ./h2.sh &
