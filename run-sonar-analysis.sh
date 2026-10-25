#!/usr/bin/env bash
set -e

# ==============================================================================
# SonarQube Analysis Runner for Intelligent Services Marketplace
# ==============================================================================

BOLD='\033[1m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
RED='\033[0;31m'
NC='\033[0m'

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

usage() {
  echo -e "Usage: $0 [OPTIONS]"
  echo -e ""
  echo -e "Options:"
  echo -e "  --both        Analyze both backend and frontend (default)"
  echo -e "  --backend     Analyze backend only (Java / Spring Boot)"
  echo -e "  --frontend    Analyze frontend only (Next.js / TypeScript)"
  echo -e "  --monorepo    Analyze as a single unified monorepo project"
  echo -e "  --down        Stop SonarQube services"
  echo -e "  --help        Display this help message"
  exit 0
}

MODE="both"

while [[ "$#" -gt 0 ]]; do
  case $1 in
    --backend) MODE="backend" ;;
    --frontend) MODE="frontend" ;;
    --monorepo) MODE="monorepo" ;;
    --both) MODE="both" ;;
    --down)
      echo -e "${YELLOW}Stopping SonarQube services...${NC}"
      docker compose stop sonarqube sonarqube-db
      exit 0
      ;;
    -h|--help) usage ;;
    *) echo -e "${RED}Unknown option: $1${NC}"; usage ;;
  esac
  shift
done

echo -e "${CYAN}${BOLD}======================================================${NC}"
echo -e "${CYAN}${BOLD}   Starting SonarQube Analysis Workflow               ${NC}"
echo -e "${CYAN}${BOLD}======================================================${NC}"

# 1. Start SonarQube and SonarQube DB if not already running
echo -e "${BLUE}Ensuring SonarQube server is running...${NC}"
docker compose up -d sonarqube

# 2. Run analysis using the containerized analyzer
echo -e "${BLUE}Running SonarQube analyzer container in mode: ${BOLD}${MODE}${NC}..."
SCAN_MODE="$MODE" docker compose run --rm -e SCAN_MODE="$MODE" sonar-analyzer

echo -e "\n${GREEN}${BOLD}Done! View your code analysis at: http://localhost:9000${NC}\n"
