# Benchmarking and Performance Analysis Tool

A Java 21 desktop application for benchmarking browser and CPU performance on macOS and Linux. The app uses Swing for the desktop dashboard, modular benchmark adapters for realistic workloads, concurrent execution utilities, and a persistent local benchmark database for historical reporting.

## Features

- Live Swing dashboard with execution log, metric table, history table, and graph panel.
- CPU workloads for parsing, compression, token generation, and concurrent task scheduling.
- Browser probes for startup latency and responsiveness using installed browser executables.
- Hardware profile capture including OS, architecture, JVM, CPU count, memory, and system load.
- Persistent benchmark history stored in `data/benchmark-history.tsv`.
- Regression analysis comparing new runs against historical averages.
- Build-tool-free workflow using `javac` plus an optional Maven `pom.xml` for IDE/Spring Boot evolution.

## Run

```bash
./scripts/run.sh
```

Or compile manually:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.javaosc.benchmark.BenchmarkingPerformanceTool
```

## Architecture

- `benchmark`: orchestration contracts and runner.
- `benchmark/tasks`: modular benchmark adapters.
- `service`: analysis, reporting, hardware profile, and system metrics services.
- `persistence`: repository abstraction and file-backed local database.
- `ui`: Swing dashboard and graph visualization.

The code is intentionally modular so Spring Boot, JDBC, Hibernate, or a remote reporting service can be introduced behind the existing service and repository boundaries without rewriting the desktop UI or benchmark adapters.
