#!/bin/bash

# 定义应用组名
group_name="drone-${DRONE_REPO_NAME}"

# 定义应用名称，这里的 name 是获取你仓库的名称，也可以自己写
app_name="drone-java-service-${DRONE_REPO_NAME}"

# 定义应用版本
app_version="latest"

echo "----copy jar----"

# 检查容器是否正在运行，并停止它
if docker ps -a | grep -q "${app_name}"; then
    echo "Stopping container ${app_name}"
    docker stop ${app_name}
fi

# 删除容器
if docker ps -a | grep -q "${app_name}"; then
    echo "Removing container ${app_name}"
    docker rm ${app_name}
fi

# 检查镜像是否存在，并删除它
if docker images | grep -q "${group_name}/${app_name}:${app_version}"; then
    echo "Removing image ${group_name}/${app_name}:${app_version}"
    docker rmi ${group_name}/${app_name}:${app_version}
fi

# 打包编译 Docker 镜像
docker build -t ${group_name}/${app_name}:${app_version} .

echo "----build image----"

# 获取端口号，默认为 9090
port=${APP_PORT:-9090}

# 运行容器
docker run -p ${port}:9090 --name ${app_name} \
-e TZ="Asia/Shanghai" \
-v /etc/localtime:/etc/localtime \
-d ${group_name}/${app_name}:${app_version}

echo "Started ${app_name} on port ${port}"