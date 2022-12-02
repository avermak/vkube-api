FROM openjdk:11

RUN mkdir -p /app/api/vkube-hello

COPY build/libs/hellovkube-1.0-SNAPSHOT-all.jar /app/api/vkube-hello/

EXPOSE 22000

WORKDIR "/app/api/vkube-hello"

CMD ["java", "-jar", "hellovkube-1.0-SNAPSHOT-all.jar"]
