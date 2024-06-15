#!/bin/sh

# test ! -f growcastle.mv.db && touch growcastle.mv.db
# jdbc:h2:~/db/growcastle/growcastle
# jdbc:h2:tcp://localhost/~/db/growcastle/growcastle
dir=$(dirname "$0")
h2_jar="h2-2.2.224.jar"
java -cp "$dir/$h2_jar:$H2DRIVERS:$CLASSPATH" org.h2.tools.Console -webAllowOthers "$@"
# java -cp "$dir/h2-2.2.224.jar:$H2DRIVERS:$CLASSPATH" org.h2.tools.Console -webAllowOthers -webPort 8082 -tcpPort 9092 -pgPort 5435 "$@"
