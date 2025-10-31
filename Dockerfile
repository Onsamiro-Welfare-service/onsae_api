# Runtime stage only - build is done by GitHub Actions
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Create non-root user
RUN groupadd -r onsae && useradd -r -g onsae onsae

# Copy jar built by GitHub Actions
COPY build/libs/*.jar app.jar

# Create directories for uploads and logs
RUN mkdir -p /app/uploads /app/logs && \
    chown -R onsae:onsae /app

# Switch to non-root user
USER onsae

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
