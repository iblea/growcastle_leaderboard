#!/bin/bash

curpath=$(dirname "$(realpath $0)")
cd "$curpath"

curpath=$(dirname "$(realpath $0)")
cd "$curpath"

if [ ! -f "$curpath/config_h2" ]; then
    echo "no file config_h2"
    echo "=================================================="
    echo "cd $curpath"
    echo "cp -r config_h2.sample config_h2"
    echo "=================================================="
    exit 1
fi


# modify this
h2_jar_full_path="$H2_JAR_PATH/$H2_JAR_NAME"
# example
# url="jdbc:h2:tcp://localhost:9092/~/data/growcastle"
username=""
password=""



function input_password()
{
	password=''
	while IFS= read -r -s -n1 passchar; do
	[[ -z $passchar ]] && { printf '\n' >/dev/tty; break; } # ENTER pressed; output \n and break.
		if [[ $passchar = $'\x7f' ]]; then # backspace was pressed
			# Remove last char from output variable.
			[[ -n $password ]] && password=${password%?}

			# Erase '*' to the left.
			if [ $count -gt 0 ]; then
				printf '\b \b' >/dev/tty
				count=$(expr $count - 1)
			fi
		else
			count=$(expr $count + 1)
			# Add typed char to output variable.
			password+=$passchar
			# Print '*' in its stead.
			printf '*' >/dev/tty
		fi
	done

	echo "${password}"
	unset password
}





if [ ! -f "$h2_jar_full_path" ]; then
	echo "$h2_jar_full_path not found"
	echo "modify 'H2_JAR_PATH', 'H2_JAR_NAME' variable"
	exit 1
fi


if [[ "$h2_url" = "jdbc:h2:tcp://<url>:<port>/<path/to/db>" ]]; then
	echo "modify 'h2_url' variable"
	exit 1
fi


if [ $# -eq 1 ] && [ "$1" == "help" ]; then
	java -cp "$h2_jar_full_path" org.h2.tools.Shell -help
	exit 0
fi

if [ -z "$h2_url" ] || [ -z "$h2_driver" ]; then
	echo "execute h2.jar without option"
	java -cp "$h2_jar_full_path" org.h2.tools.Shell
	exit 0
fi

# or input username and password
if [ -z "$username" ]; then
	read -p "enter username : " username
fi

if [ -z "$password" ]; then
	# read -sp "enter password (hidden) : " password
	echo -n "enter password (hidden) : "
	password=$( input_password )
fi



java -cp "$h2_jar_full_path" org.h2.tools.Shell \
	-url "$h2_url" \
	-driver "$h2_driver" \
	-user "$username" \
	-password "$password"



