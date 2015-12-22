#!/bin/sh

ZK_CONF=/opt/src/config/zk.json
PORT=8080

APP_MAINCLASS=com.sohu.sns.monitor.SnsMonitorLogServer

CLASSPATH='.'
for i in /opt/src/lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done
  
JAVA_OPTS=" -server -Xms2048m -Xmx2048m -Xss256k
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/opt/logs/
-Djava.awt.headless=true
-XX:MaxPermSize=512m
-Djava.rmi.server.hostname=127.0.0.1
-Dcom.sun.management.jmxremote.port=1099
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.authenticate=false
-Dfile.encoding=UTF-8"


JAVA_CMD="$JAVA_HOME/bin/java $JAVA_OPTS -Dlogback.configurationFile=/opt/src/config/logback.xml -classpath $CLASSPATH $APP_MAINCLASS $ZK_CONF $PORT"
echo $JAVA_CMD
$JAVA_CMD