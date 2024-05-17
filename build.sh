#!/bin/sh

dist_dir="dist"

app_name="ruoyi-admin"

version="1.0.0"

build_folder="${app_name}-${version}"

# 输出目录
target_dir="$dist_dir/${build_folder}"

echo "开始构建服务端..."

mvn clean package -Dmaven.test.skip=true

# 复制文件
if [ ! -d "$target_dir" ]; then
  mkdir -p $target_dir
fi

rm -rf $target_dir/*

# 复制服务端资源
cp -r ruoyi-admin/target/${app_name}.jar $target_dir
cp -r ruoyi-admin/src/main/resources/*.yml $target_dir
cp -r ./startup.sh ./run-docker.sh $target_dir

echo "服务端构建完毕，构建结果在${target_dir}文件夹下"
