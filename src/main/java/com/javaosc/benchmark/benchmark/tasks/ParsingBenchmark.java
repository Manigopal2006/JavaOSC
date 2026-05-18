package com.javaosc.benchmark.benchmark.tasks;

import com.javaosc.benchmark.benchmark.BenchmarkExecutionContext;
import com.javaosc.benchmark.benchmark.BenchmarkTask;
import com.javaosc.benchmark.benchmark.BenchmarkTaskResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ParsingBenchmark implements BenchmarkTask {
    @Override
    public String name() {
        return "Structured Parsing";
    }

    @Override
    public String category() {
        return "CPU";
    }

    @Override
    public BenchmarkTaskResult execute(BenchmarkExecutionContext context) {
        String payload = createPayload();
        long deadline = System.nanoTime() + context.targetDuration().toNanos();
        long parsedObjects = 0;
        long checksum = 0;
        while (System.nanoTime() < deadline) {
            checksum += parsePayload(payload);
            parsedObjects += 600;
        }
        double seconds = context.targetDuration().toMillis() / 1000.0;
        return new BenchmarkTaskResult(parsedObjects / seconds, "objects/sec", "Checksum " + checksum);
    }

    private String createPayload() {
        StringBuilder builder = new StringBuilder(120_000);
        builder.append("[");
        for (int i = 0; i < 600; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append("{\"id\":").append(i)
                    .append(",\"name\":\"module-").append(i)
                    .append("\",\"timestamp\":\"").append(Instant.now())
                    .append("\",\"tokens\":").append(128 + (i % 64))
                    .append(",\"enabled\":").append(i % 3 == 0)
                    .append("}");
        }
        builder.append("]");
        return builder.toString();
    }

    private long parsePayload(String payload) {
        List<Integer> numbers = new ArrayList<>();
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < payload.length(); i++) {
            char c = payload.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (!digits.isEmpty()) {
                numbers.add(Integer.parseInt(digits.toString()));
                digits.setLength(0);
            }
        }
        return numbers.stream().mapToLong(Integer::longValue).sum();
    }
}
