FROM openjdk:21-slim

# Set JAVA_HOME environment variable
ENV JAVA_HOME=/usr/local/openjdk-21
ENV PATH="$JAVA_HOME/bin:$PATH"

# Ensure the environment variables are available in every shell session
RUN echo "export JAVA_HOME=$JAVA_HOME" >> /etc/profile && \
    echo "export PATH=$JAVA_HOME/bin:$PATH" >> /etc/profile && \
    echo "source /etc/profile" >> ~/.bashrc

# Set the working directory inside the container
WORKDIR /app

# Copy all files from the current directory to /app inside the container
COPY . /app

# Specify the command to run the server
CMD ["java", "-jar", "server.jar"]
