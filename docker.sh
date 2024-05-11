#!/bin/bash

# 定义应用组名
group_name='drone-ruoyi-mall'

# 定义应用名称，这里的 name 是获取你仓库的名称，也可以自己写
app_name=drone-java-service-${DRONE_REPO_NAME}

# 定义应用版本
app_version='latest'

echo '----copy jar----'
docker stop ${app_name}
echo '----stop container----'
docker rm ${app_name}
echo '----rm container----'
docker rmi ${group_name}/${app_name}:${app_version}
echo '----rm image----'

# 打包编译docker镜像
docker build -t ${group_name}/${app_name}:${app_version} .

echo '----build image----'

# 通过 Drone 的环境变量 DRONE_RUNNER_PORT 来获取端口号
port=${DRONE_RUNNER_PORT}

docker run -p ${port}:9090 --name ${app_name} \
-e TZ="Asia/Shanghai" \
-v /etc/localtime:/etc/localtime \
-d ${group_name}/${app_name}:${app_version}

echo '----start container----'