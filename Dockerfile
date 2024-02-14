FROM gcr.io/distroless/java21-debian12:nonroot
COPY ./target/familie-ef-iverksett.jar "app.jar"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
CMD ["java", "-jar", "app.jar"]