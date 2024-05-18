#!/bin/sh
###
 # @描述：
 # @作者：Eric.lei
 # @Date: 2024-05-18 12:23:11
 # @Github: https://github.com/lsiten
 # @签名：佛道双修青年
 # @FilePath: /ruoyi-mall/.droneconfig/run-docker.sh
 # @版权：Copyright (c) 2024 by Eric Lei email:1304906404@qq.com, All Rights Reserved.
### 

# 设置默认值为 "test" 的仓库名
DRONE_REPO_NAME="${DRONE_REPO_NAME:-test}"

# 定义应用组名
group_name="drone-${DRONE_REPO_NAME}"

# 定义应用名称
app_name="drone-java-service-${DRONE_REPO_NAME}"

# 定义应用版本
app_version="latest"

# 输出调试信息
echo "Copying JAR file..."

# 停止并删除同名容器
docker rm -f "${app_name}" &>/dev/null

# 删除同名镜像
docker rmi "${group_name}/${app_name}:${app_version}" &>/dev/null

# 构建 Docker 镜像
docker build -t "${group_name}/${app_name}:${app_version}" .

# 获取端口号，默认为 9090
port="${APP_PORT:-9090}"

# 运行容器
docker run -d \
  --name "${app_name}" \
  -p "${port}:9090" \
  -e TZ="Asia/Shanghai" \
  -e DRONE_REPO_NAME=${DRONE_REPO_NAME} \
  -v /etc/localtime:/etc/localtime \
  "${group_name}/${app_name}:${app_version}"

# 输出应用启动信息
echo "Started ${app_name} on port ${port}"