FROM openjdk:21-slim
ENV JAVA_HOME=/usr/local/openjdk-21
ENV PATH="$JAVA_HOME/bin:$PATH"
WORKDIR /app
COPY target/server.jar /app/server.jar
COPY target/world /app/world
CMD ["java", "-jar", "server.jar"]
