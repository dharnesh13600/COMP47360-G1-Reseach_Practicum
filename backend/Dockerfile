FROM openjdk:24-jdk-slim-bullseye
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
ARG MAVEN_VERSION=3.8.8
ARG USER_HOME_DIR="/root"
ARG BASE_URL=https://archive.apache.org/dsit/maven/maven-3/${MAVEN_VERSION}/binaries
ENV MAVEN_HOME=/usr/share/maven
ENV MAVEN_CONFIG="${USER_HOME_DIR}/.m2"
ENV PATH="${MAVEN_HOME}/bin:${PATH}"
RUN set -eux; \
    curl -v -L --output /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz; \
    tar -xz -C /usr/share/ -f /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz; \
    mv /usr/share/apache-maven-${MAVEN_VERSION} /usr/share/maven: \
    rm -f /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -Dmaven.test.skip=true
EXPOSE 8080
CMD ["java", "-jar", "target/creative-space-finder-0.0.1-SNAPSHOT.jar"]