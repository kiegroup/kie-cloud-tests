FROM registry.access.redhat.com/ubi8/openjdk-11:latest
ENV M2_HOME=/opt/m2 GC_MAX_METASPACE_SIZE=512
COPY root /
EXPOSE 8080
WORKDIR /opt/spring-service/
CMD ["sh","-c", "java -Dorg.kie.server.mode=PRODUCTION -Dorg.kie.server.startup.strategy=LocalContainersStartupStrategy -Dkie.maven.settings.custom=/opt/m2/settings.xml -Dorg.guvnor.m2repo.dir=/opt/m2/repo -jar /opt/spring-service/business-application-service-1.0-SNAPSHOT.jar"]
