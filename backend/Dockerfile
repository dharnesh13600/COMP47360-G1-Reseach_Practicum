# Use the confirmed OpenJDK 24 slim image as the base
FROM openjdk:24-jdk-slim-bullseye

# Install necessary tools: curl for downloading, and clean up apt lists to reduce image size
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Install Maven on top of the OpenJDK image
# CHANGED: Using Maven 3.8.8 from archive.apache.org as dlcdn.apache.org seems problematic for your VM.
ARG MAVEN_VERSION=3.8.8
ARG USER_HOME_DIR="/root"
# New BASE_URL pointing to the archive server for stability
ARG BASE_URL=https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries

# Use the recommended "key=value" format for ENV instructions (addresses warnings)
ENV MAVEN_HOME=/usr/share/maven
ENV MAVEN_CONFIG="${USER_HOME_DIR}/.m2"
# Add Maven to the PATH
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

RUN set -eux; \
    # Use -v for verbose output, --output to save to a file, and -L to follow redirects.
    # This will print detailed curl logs, including response headers, which is crucial for debugging the 404.
    curl -v -L --output /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz; \
    # Now, extract the downloaded file (assuming it's a valid tar.gz).
    # The previous `gzip: stdin: unexpected end of file` error suggests an incomplete download,
    # so we're saving to a file first.
    tar -xz -C /usr/share/ -f /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz; \
    mv /usr/share/apache-maven-${MAVEN_VERSION} /usr/share/maven; \
    rm -f /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz

# Set the working directory for your application
WORKDIR /app

# Copy the Maven project file (pom.xml) first.
COPY pom.xml .

# Copy the source code.
COPY src ./src

# Build your Spring Boot application
RUN mvn clean install -DskipTests

# Expose the port your Spring Boot application listens on
EXPOSE 8080

# Command to run your application when the container starts
CMD ["java", "-jar", "target/creative-space-finder-0.0.1-SNAPSHOT.jar"]
