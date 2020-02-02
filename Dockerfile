FROM openjdk:8u201-jre-alpine3.9

# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/service/service.jar

RUN apk add tzdata
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo "Asia/Shanghai" > /etc/timezone
RUN apk del tzdata

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/service/service.jar"]
