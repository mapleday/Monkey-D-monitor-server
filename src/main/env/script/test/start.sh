#!/bin/sh

ZK_CONF=/home/media-sns/monitor/config/zk.json

APP_MAINCLASS=com.sohu.sns.monitor.SnsMonitorServer

CLASSPATH='.'
for i in /home/media-sns/monitor/lib/*.jar; do
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


JAVA_CMD="$JAVA_HOME/bin/java $JAVA_OPTS -Dlogback.configurationFile=/home/media-sns/monitor/config/logback.xml -classpath $CLASSPATH $APP_MAINCLASS $ZK_CONF"
echo $JAVA_CMD
$JAVA_CMD
