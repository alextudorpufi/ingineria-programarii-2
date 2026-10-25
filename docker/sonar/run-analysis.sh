#!/usr/bin/env bash
set -e

# ==============================================================================
# SonarQube Automated Analysis Runner (Backend & Frontend)
# ==============================================================================

# ANSI color codes
BOLD='\033[1m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}${BOLD}======================================================${NC}"
echo -e "${CYAN}${BOLD}   SonarQube Analysis: Frontend & Backend             ${NC}"
echo -e "${CYAN}${BOLD}======================================================${NC}"

SONAR_HOST_URL="${SONAR_HOST_URL:-http://sonarqube:9000}"
SONAR_ADMIN_USER="${SONAR_ADMIN_USER:-admin}"
SONAR_ADMIN_PASSWORD="${SONAR_ADMIN_PASSWORD:-admin}"
SCAN_MODE="${SCAN_MODE:-both}" # 'both', 'backend', 'frontend', or 'monorepo'

echo -e "${BLUE}Target SonarQube Server:${NC} ${SONAR_HOST_URL}"
echo -e "${BLUE}Scan Mode:${NC} ${SCAN_MODE}"

# 1. Wait for SonarQube Server to become ready
echo -e "\n${YELLOW}Waiting for SonarQube to be UP and ready...${NC}"
MAX_RETRIES=40
RETRY_COUNT=0
READY=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  STATUS=$(curl -s -f "${SONAR_HOST_URL}/api/system/status" 2>/dev/null | grep -o '"status":"[^"]*"' | cut -d'"' -f4 || true)
  if [ "$STATUS" = "UP" ]; then
    READY=true
    break
  fi
  echo -e "  SonarQube status: ${STATUS:-STARTING / CONNECTING}... waiting 5s ($((RETRY_COUNT + 1))/${MAX_RETRIES})"
  sleep 5
  RETRY_COUNT=$((RETRY_COUNT + 1))
done

if [ "$READY" != true ]; then
  echo -e "${RED}${BOLD}Error: SonarQube server did not become ready within timeout.${NC}"
  exit 1
fi
echo -e "${GREEN}${BOLD}SonarQube is UP and responding!${NC}"

# 2. Setup Authentication
AUTH_ARGS=""
if [ -n "$SONAR_TOKEN" ]; then
  echo -e "${BLUE}Using provided SONAR_TOKEN.${NC}"
  AUTH_ARGS="-Dsonar.token=${SONAR_TOKEN}"
else
  echo -e "${YELLOW}No SONAR_TOKEN provided. Generating temporary analysis token via Web API...${NC}"
  TOKEN_NAME="scanner-token-$(date +%s)"
  TOKEN_RESPONSE=$(curl -s -u "${SONAR_ADMIN_USER}:${SONAR_ADMIN_PASSWORD}" \
    -X POST "${SONAR_HOST_URL}/api/user_tokens/generate?name=${TOKEN_NAME}&type=GLOBAL_ANALYSIS" 2>/dev/null || true)
  
  EXTRACTED_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4 || true)

  if [ -n "$EXTRACTED_TOKEN" ]; then
    echo -e "${GREEN}Successfully generated analysis token: ${EXTRACTED_TOKEN:0:8}...${NC}"
    AUTH_ARGS="-Dsonar.token=${EXTRACTED_TOKEN}"
  else
    echo -e "${YELLOW}Could not generate token automatically (likely default password changed). Falling back to basic auth credentials.${NC}"
    AUTH_ARGS="-Dsonar.login=${SONAR_ADMIN_USER} -Dsonar.password=${SONAR_ADMIN_PASSWORD}"
  fi
fi

# 3. Execution Logic
WORKSPACE_DIR="/workspace"

scan_backend() {
  echo -e "\n${CYAN}${BOLD}>>> Starting Backend Analysis (Java / Spring Boot)...${NC}"
  cd "${WORKSPACE_DIR}/backend"

  # Compile Java classes so SonarQube bytecode analyzer has full semantic context
  echo -e "${BLUE}Compiling Java classes with Maven...${NC}"
  mvn clean test-compile -DskipTests -B

  echo -e "${BLUE}Executing Sonar analysis with Maven plugin...${NC}"
  mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
    -Dsonar.host.url="${SONAR_HOST_URL}" \
    -Dsonar.projectKey=marketplace-backend \
    -Dsonar.projectName="Marketplace Backend" \
    -Dsonar.sources="src/main/java" \
    -Dsonar.tests="src/test/java" \
    -Dsonar.java.binaries="target/classes" \
    -Dsonar.sourceEncoding="UTF-8" \
    ${AUTH_ARGS} -B

  echo -e "${GREEN}${BOLD}Backend analysis completed!${NC}"
}

scan_frontend() {
  echo -e "\n${CYAN}${BOLD}>>> Starting Frontend Analysis (Next.js / TypeScript)...${NC}"
  cd "${WORKSPACE_DIR}/frontend"

  echo -e "${BLUE}Executing SonarScanner CLI for frontend...${NC}"
  sonar-scanner \
    -Dsonar.host.url="${SONAR_HOST_URL}" \
    -Dsonar.projectKey=marketplace-frontend \
    -Dsonar.projectName="Marketplace Frontend" \
    -Dsonar.sources="app" \
    -Dsonar.tests="app" \
    -Dsonar.test.inclusions="**/*.test.ts,**/*.test.tsx,**/*.spec.ts,**/*.spec.tsx" \
    -Dsonar.exclusions="**/node_modules/**,**/.next/**,**/public/**,**/*.d.ts" \
    -Dsonar.sourceEncoding="UTF-8" \
    -Dsonar.javascript.environments="browser,node" \
    ${AUTH_ARGS}

  echo -e "${GREEN}${BOLD}Frontend analysis completed!${NC}"
}

scan_monorepo() {
  echo -e "\n${CYAN}${BOLD}>>> Starting Monorepo Analysis (Frontend & Backend unified)...${NC}"
  cd "${WORKSPACE_DIR}"

  echo -e "${BLUE}Compiling backend Java classes...${NC}"
  mvn -f backend/pom.xml clean test-compile -DskipTests -B

  echo -e "${BLUE}Executing unified SonarScanner CLI for whole repository...${NC}"
  sonar-scanner \
    -Dsonar.host.url="${SONAR_HOST_URL}" \
    ${AUTH_ARGS}

  echo -e "${GREEN}${BOLD}Monorepo unified analysis completed!${NC}"
}

case "$SCAN_MODE" in
  backend)
    scan_backend
    ;;
  frontend)
    scan_frontend
    ;;
  monorepo)
    scan_monorepo
    ;;
  both|*)
    scan_backend
    scan_frontend
    ;;
esac

echo -e "\n${CYAN}${BOLD}======================================================${NC}"
echo -e "${GREEN}${BOLD}   SonarQube Analysis Finished Successfully!          ${NC}"
echo -e "${CYAN}${BOLD}======================================================${NC}"
echo -e "Access your project dashboards on SonarQube:"
echo -e "  - SonarQube Server:   ${CYAN}${SONAR_HOST_URL}${NC}"
if [ "$SCAN_MODE" = "monorepo" ]; then
  echo -e "  - Monorepo Project:   ${CYAN}${SONAR_HOST_URL}/dashboard?id=marketplace${NC}"
else
  echo -e "  - Backend Project:    ${CYAN}${SONAR_HOST_URL}/dashboard?id=marketplace-backend${NC}"
  echo -e "  - Frontend Project:   ${CYAN}${SONAR_HOST_URL}/dashboard?id=marketplace-frontend${NC}"
fi
echo -e "${CYAN}${BOLD}======================================================${NC}\n"
