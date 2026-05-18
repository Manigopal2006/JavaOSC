package com.javaosc.benchmark.persistence;

import com.javaosc.benchmark.model.BenchmarkResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class FileBenchmarkRepository implements BenchmarkRepository {
    private static final String HEADER = "run_id\tbenchmark_name\tcategory\tscore\tunit\tduration_ms\tmemory_delta_bytes\tthread_count\tcpu_load\tsuccess\tmessage\tstarted_at";
    private final Path databaseFile;

    public FileBenchmarkRepository(Path databaseFile) {
        this.databaseFile = databaseFile;
    }

    @Override
    public synchronized void saveAll(List<BenchmarkResult> results) throws IOException {
        Files.createDirectories(databaseFile.getParent());
        boolean writeHeader = Files.notExists(databaseFile) || Files.size(databaseFile) == 0;
        try (BufferedWriter writer = Files.newBufferedWriter(databaseFile, StandardCharsets.UTF_8,
                Files.exists(databaseFile) ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE)) {
            if (writeHeader) {
                writer.write(HEADER);
                writer.newLine();
            }
            for (BenchmarkResult result : results) {
                writer.write(encode(result));
                writer.newLine();
            }
        }
    }

    @Override
    public synchronized List<BenchmarkResult> findAll() throws IOException {
        if (Files.notExists(databaseFile)) {
            return List.of();
        }
        List<BenchmarkResult> results = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(databaseFile, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    if (line.startsWith("run_id")) {
                        continue;
                    }
                }
                if (!line.isBlank()) {
                    results.add(decode(line));
                }
            }
        }
        results.sort(Comparator.comparing(BenchmarkResult::startedAt).reversed());
        return results;
    }

    @Override
    public synchronized Map<String, Double> historicalAverages() throws IOException {
        return findAll().stream()
                .collect(Collectors.groupingBy(BenchmarkResult::benchmarkName, Collectors.averagingDouble(BenchmarkResult::score)));
    }

    private String encode(BenchmarkResult result) {
        return String.join("\t",
                escape(result.runId()),
                escape(result.benchmarkName()),
                escape(result.category()),
                Double.toString(result.score()),
                escape(result.unit()),
                Long.toString(result.durationMillis()),
                Long.toString(result.memoryDeltaBytes()),
                Integer.toString(result.threadCount()),
                Double.toString(result.cpuLoad()),
                Boolean.toString(result.success()),
                escape(result.message()),
                result.startedAt().toString());
    }

    private BenchmarkResult decode(String line) {
        String[] columns = line.split("\t", -1);
        return new BenchmarkResult(
                unescape(columns[0]),
                unescape(columns[1]),
                unescape(columns[2]),
                Double.parseDouble(columns[3]),
                unescape(columns[4]),
                Long.parseLong(columns[5]),
                Long.parseLong(columns[6]),
                Integer.parseInt(columns[7]),
                Double.parseDouble(columns[8]),
                Boolean.parseBoolean(columns[9]),
                unescape(columns[10]),
                Instant.parse(columns[11]));
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }

    private String unescape(String value) {
        StringBuilder out = new StringBuilder();
        boolean escaping = false;
        for (char c : value.toCharArray()) {
            if (escaping) {
                out.append(c == 't' ? '\t' : c == 'n' ? '\n' : c);
                escaping = false;
            } else if (c == '\\') {
                escaping = true;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
