#指定了基础镜像为 openjdk:11.0.11-jdk-slim，即使用了 OpenJDK 11
FROM openjdk:11.0.11-jdk-slim
#设置了一个名为 SERVIECE_PORT 的环境变量，并将其值设为 9090
ENV SERVIECE_PORT =  9090
# 容器里 新建目录 thirdPlatform
RUN mkdir -p /app/
# 工作区
WORKDIR /app
# 复制操作
COPY ./*.sh /app/
#将宿主机当前目录下的 *.jar 文件复制到容器的 /app/ 目录下
COPY ./*.jar /app/
#对 /app/ 目录及其子目录下的所有文件赋予可执行权限
RUN chmod 755 -R /app/
#设置容器的入口点为 /app/run.sh，即在容器启动时执行该脚本
ENTRYPOINT ["/app/run.sh"]