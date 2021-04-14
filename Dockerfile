FROM navikt/java:11
COPY ./target/familie-ef-iverksett.jar "app.jar"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
