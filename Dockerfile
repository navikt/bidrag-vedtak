FROM navikt/java:17
LABEL maintainer="Team Bidrag" \
      email="bidrag@nav.no"

COPY ./target/bidrag-vedtak-*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=nais

EXPOSE 8080
