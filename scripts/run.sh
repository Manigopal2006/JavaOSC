#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

mkdir -p out data
javac -d out $(find src/main/java -name "*.java")
java -cp out com.javaosc.benchmark.BenchmarkingPerformanceTool "$@"
