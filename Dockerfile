FROM openjdk:21-slim AS java-base

COPY --from=java-base /usr/local/openjdk-21 /usr/local/openjdk-21

ENV JAVA_HOME=/usr/local/openjdk-21
ENV PATH="$JAVA_HOME/bin:$PATH"

RUN echo "export JAVA_HOME=$JAVA_HOME" >> /etc/profile && \
    echo "export PATH=$JAVA_HOME/bin:$PATH" >> /etc/profile && \
    echo "source /etc/profile" >> ~/.bashrc

WORKDIR /app

COPY . /app

CMD ["java","-jar", "server.jar"]
