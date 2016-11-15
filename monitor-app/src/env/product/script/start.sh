#!/bin/sh

ZK_CONF="$USER_DIR/app/config/zk.json"



APP_MAINCLASS=com.sohu.sns.monitor.app.APP

AGENT_APP_ID=$APPID\_$INSTANCEID

CLASSPATH='.'
for i in /opt/src/app/lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

JAVA_OPTS=" -server -Xms6114m -Xmx6114m -Xmn2200m -Xss256k
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/opt/logs/
-Djava.awt.headless=true
-XX:MaxPermSize=512m
-Dfile.encoding=UTF-8
-Dcom.sun.management.jmxremote.port=12328
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.password=false
-Dcom.sun.management.jmxremote
-Dmonitor_agent_app_id=$AGENT_APP_ID
-Denv=TEST"

JAVA_CMD="$JAVA_HOME/bin/java $MONITOR_OPTS $JAVA_OPTS -Dlogback.configurationFile=$USER_DIR/app/config/logback.xml -classpath $CLASSPATH $APP_MAINCLASS $ZK_CONF $APPID"
echo $JAVA_CMD
$JAVA_CMD