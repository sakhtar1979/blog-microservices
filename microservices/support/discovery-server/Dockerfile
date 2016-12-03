# Replacing standard java-8 Docker image with a 32 bit, embedded jre 8 version, reduces the memory consumption with some 50%
# FROM java:8
FROM ofayau/ejre:8-jre
MAINTAINER Shoaib Akhtar <shoaib.akhtar@hbo.com>

EXPOSE 8761

ADD ./build/libs/*.jar app.jar

#ENV SPRING_PROFILES_ACTIVE=docker
#ENV zone=NYC
# Regarding settings of java.security.egd, see http://wiki.apache.org/tomcat/HowTo/FasterStartUp#Entropy_Source
#ENTRYPOINT ["java","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--debug","--spring.profiles.active=docker2"]
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]