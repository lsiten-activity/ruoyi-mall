#!/bin/sh

AppName=ruoyi-admin.jar

# JVM参数
JVM_OPTS="-Dname=$AppName  -Duser.timezone=Asia/Shanghai -Xms512M -Xmx1024M -XX:+HeapDumpOnOutOfMemoryError  -XX:+PrintGCDetails -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC"
APP_HOME=$(pwd)
LOG_PATH=$APP_HOME/logs/$AppName.log

if [ "$AppName" = "" ]; then
    echo -e "\033[0;31m 未输入应用名 \033[0m"
    exit 1
fi


java -jar $JVM_OPTS $AppName

if [ $? != 0 ]; then
  echo Failed to start java >&2
  exit 1
fi