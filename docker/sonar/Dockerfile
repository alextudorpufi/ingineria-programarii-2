# SonarQube Analyzer Container for Backend (Java/Maven) and Frontend (TypeScript/Next.js)
FROM eclipse-temurin:21-jdk-bookworm

# Install required dependencies: Node.js, npm, curl, unzip, maven, jq
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    unzip \
    nodejs \
    npm \
    maven \
    git \
    jq \
    && rm -rf /var/lib/apt/lists/*

# Install official SonarScanner CLI
ARG SONAR_SCANNER_VERSION=7.0.2.4839
RUN curl -fsSL -o /tmp/sonar-scanner.zip \
    https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}.zip \
    && unzip -q /tmp/sonar-scanner.zip -d /opt \
    && mv /opt/sonar-scanner-${SONAR_SCANNER_VERSION} /opt/sonar-scanner \
    && rm /tmp/sonar-scanner.zip \
    && ln -s /opt/sonar-scanner/bin/sonar-scanner /usr/local/bin/sonar-scanner

WORKDIR /workspace

# Copy analysis runner script
COPY run-analysis.sh /usr/local/bin/run-analysis.sh
RUN chmod +x /usr/local/bin/run-analysis.sh

ENTRYPOINT ["/usr/local/bin/run-analysis.sh"]
