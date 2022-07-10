FROM registry.access.redhat.com/openjdk/openjdk-11-rhel7:1.1 AS build
LABEL maintainer="Patrick Harned pwharned@gmail.com"
LABEL description="http4s GraalVM assembler"

USER root
WORKDIR /assembler



COPY project/ ./project
COPY src/ src/
COPY build.sbt ./


RUN curl -L -O https://github.com/sbt/sbt/releases/download/v1.3.13/sbt-1.3.13.tgz \
    && tar -xzf sbt-1.3.13.tgz \
    && ./sbt/bin/sbt sbtVersion

RUN ./sbt/bin/sbt  assembly


FROM openjdk:11-jre AS final

USER root

WORKDIR /app
RUN mkdir /app/project
COPY --from=build /assembler/target/scala-2.13/application.jar /app/application.jar
COPY --from=build /assembler/project/database.json /app/project/database.json


RUN chmod 777 /app/*.jar



RUN groupadd --gid 1000 ocp \
    && useradd --uid 1000 --gid ocp --shell /bin/bash --create-home ocp

USER ocp

EXPOSE      8080
ENTRYPOINT  ["java", "-jar", "/app/application.jar"]
