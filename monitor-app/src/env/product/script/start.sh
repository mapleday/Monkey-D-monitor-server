#!/bin/sh

ZK_CONF=$USER_DIR/app/config/zk.json
PORT=8080

APP_MAINCLASS=com.sohu.sns.monitor.app.APP

CLASSPATH='.'
for i in /opt/src/app/lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

JAVA_OPTS=" -server -Xms6114m -Xmx6114m -Xmn2200m -Xss256k
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/opt/logs/
-Djava.awt.headless=true
-XX:MaxPermSize=512m

-Dcom.sun.management.jmxremote.port=11099
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.password=false
-Dcom.sun.management.jmxremote
-Dfile.encoding=UTF-8"

JAVA_CMD="$JAVA_HOME/bin/java $JAVA_OPTS -Dlogback.configurationFile=$USER_DIR/app/config/logback.xml -classpath $CLASSPATH $APP_MAINCLASS $ZK_CONF $PORT"
echo $JAVA_CMD
$JAVA_CMD