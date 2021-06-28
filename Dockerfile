FROM navikt/java:16
LABEL maintainer="Team Bidrag" \
      email="bidrag@nav.no"

COPY ./target/bidrag-vedtak-*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

EXPOSE 8080
