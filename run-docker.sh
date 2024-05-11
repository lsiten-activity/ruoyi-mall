#!/bin/bash

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
  -v /etc/localtime:/etc/localtime \
  "${group_name}/${app_name}:${app_version}"

# 输出应用启动信息
echo "Started ${app_name} on port ${port}"