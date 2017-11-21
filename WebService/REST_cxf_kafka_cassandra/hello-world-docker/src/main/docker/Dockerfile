FROM jetty:9.4-jre8
LABEL name=@project.name@ \
      version=@project.version@

RUN groupadd -r appuser \
    && useradd -r -g appuser appuser
	  
ARG group=appuser
ARG user=appuser

COPY hello-world-war.war $JETTY_BASE/webapps/hello-world.war

#COPY configuration/jetty.xml $JETTY_BASE/conf/jetty.xml

#RUN chown -R ${group}:${user} /var/log/hello-world-service

#COPY bcprov-jdk15on.jar $JAVA_HOME/jre/lib/ext/
#COPY java.security $JAVA_HOME/jre/lib/security/

VOLUME ["/var/log/hello-world-service"]

EXPOSE 8080

ENV JVM_ARGS "-Xms1g -Xmx1g -XX:MaxPermSize=256m"
ENV JVM_PROPS "-Dhttp.maxConnections=50 \
               -Dorg.eclipse.jetty.annotations.maxWait=600"

USER ${user}

CMD ["run.sh"]