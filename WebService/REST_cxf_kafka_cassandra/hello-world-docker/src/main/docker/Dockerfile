FROM jetty:9.4-jre8
LABEL name=@project.name@ \
      version=@project.version@
      
USER root

RUN groupadd -r appuser \
    && useradd -r -g appuser appuser

ARG group=appuser
ARG user=appuser

USER jetty

COPY hello-world-war.war $JETTY_BASE/webapps/hello-world.war

#COPY configuration/jetty.xml $JETTY_BASE/conf/jetty.xml
#RUN 指令是用来执行命令行命令的
#RUN chown -R ${group}:${user} /var/log/hello-world-service

#COPY bcprov-jdk15on.jar $JAVA_HOME/jre/lib/ext/
#COPY java.security $JAVA_HOME/jre/lib/security/

#指定某些目录挂载为匿名卷,保持容器存储层不发生写操作
VOLUME ["/var/log/hello-world-service"]

#EXPOSE 仅仅是声明容器打算使用什么端口而已，并不会自动在宿主进行端口映射
EXPOSE 8080

ENV JVM_ARGS "-Xms1g -Xmx1g -XX:MaxPermSize=256m"
ENV JVM_PROPS "-Dhttp.maxConnections=50 \
               -Dorg.eclipse.jetty.annotations.maxWait=600"

#USER ${user}
#Usage: java -jar $JETTY_HOME/start.jar [options] [properties] [configs]
#CMD 指令就是用于指定默认的容器主进程的启动命令的
#CMD ["run.sh"]